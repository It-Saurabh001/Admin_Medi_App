package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.data.v1.dto.AppConfigEnums
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.data.v1.dto.ProductCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.ProductUpdateRequest
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.FormScaffold
import com.saurabh.mediadminapp.ui.components.ImageSourceSheet
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.utils.utilityFunctions.createImageUri
import com.saurabh.mediadminapp.utils.utilityFunctions.getUCropIntent
import com.yalantis.ucrop.UCrop

private val FALLBACK_SCHEDULES = listOf("OTC", "H", "H1", "X", "NARCOTIC")

/** All product fields as strings (survives rotation); `rx` is "true"/"false". */
@Stable
class ProductDraft(initial: Map<String, String>) {
    val values = mutableStateMapOf<String, String>().apply { putAll(initial) }
    var errors by mutableStateOf<Map<String, String>>(emptyMap())

    operator fun get(key: String): String = values[key].orEmpty()
    operator fun set(key: String, v: String) { values[key] = v; if (errors.containsKey(key)) errors = errors - key }

    private fun str(key: String): String? = get(key).trim().ifBlank { null }

    /** Sets [errors]; true when valid. */
    fun validate(): Boolean {
        val v = { k: String -> this[k] }
        errors = buildMap {
            if (v("name").isBlank()) put("name", "Name is required")
            val p = v("price").toDoubleOrNull()
            if (p == null || p < 0) put("price", "Enter a valid price")
            listOf("mrp" to "MRP", "purchase" to "Purchase price").forEach { (k, l) ->
                if (v(k).isNotBlank() && v(k).toDoubleOrNull()?.takeIf { it >= 0 } == null) put(k, "Enter a valid ${l.lowercase()}")
            }
            if (v("gst").isNotBlank() && v("gst").toDoubleOrNull()?.takeIf { it in 0.0..100.0 } == null) put("gst", "0 to 100")
            if (v("reorder").isNotBlank() && v("reorder").toIntOrNull()?.takeIf { it >= 0 } == null) put("reorder", "Whole number, 0 or more")
        }
        return errors.isEmpty()
    }

    fun applyServerFields(fields: Map<String, String>?) {
        if (fields.isNullOrEmpty()) return
        val map = mapOf(
            "name" to "name", "price" to "price", "generic_name" to "generic", "brand_name" to "brand", "manufacturer" to "manufacturer",
            "category" to "category", "dosage_form" to "dosage", "strength" to "strength", "pack_size" to "pack", "unit" to "unit",
            "hsn_code" to "hsn", "gst_percent" to "gst", "mrp" to "mrp", "purchase_price" to "purchase", "schedule_type" to "schedule",
            "storage_conditions" to "storage", "reorder_level" to "reorder", "barcode" to "barcode"
        )
        errors = errors + fields.mapNotNull { (k, v) -> map[k]?.let { it to v } }.toMap()
    }

    fun toCreate() = ProductCreateRequest(
        name = get("name").trim(), price = get("price").toDouble(),
        genericName = str("generic"), brandName = str("brand"), manufacturer = str("manufacturer"), category = str("category"),
        dosageForm = str("dosage"), strength = str("strength"), packSize = str("pack"), unit = str("unit"), hsnCode = str("hsn"),
        gstPercent = get("gst").toDoubleOrNull(), mrp = get("mrp").toDoubleOrNull(), purchasePrice = get("purchase").toDoubleOrNull(),
        scheduleType = str("schedule"), requiresPrescription = get("rx").toBooleanStrictOrNull(), storageConditions = str("storage"),
        reorderLevel = get("reorder").toIntOrNull(), barcode = str("barcode")
    )

    /** Only fields that differ from [o]. Cleared optional text fields are left unchanged (server cannot blank them). */
    fun toUpdate(o: Product): ProductUpdateRequest {
        fun s(key: String, old: String): String? = str(key)?.takeIf { it != old.trim() }
        fun d(key: String, old: Double?): Double? = get(key).toDoubleOrNull()?.takeIf { it != old }
        return ProductUpdateRequest(
            name = s("name", o.name), price = d("price", o.price),
            genericName = s("generic", o.genericName), brandName = s("brand", o.brandName), manufacturer = s("manufacturer", o.manufacturer),
            category = s("category", o.category), dosageForm = s("dosage", o.dosageForm), strength = s("strength", o.strength),
            packSize = s("pack", o.packSize), unit = s("unit", o.unit), hsnCode = s("hsn", o.hsnCode),
            gstPercent = d("gst", o.gstPercent), mrp = d("mrp", o.mrp), purchasePrice = d("purchase", o.purchasePrice),
            scheduleType = s("schedule", o.scheduleType),
            requiresPrescription = get("rx").toBooleanStrictOrNull()?.takeIf { it != o.requiresPrescription },
            storageConditions = s("storage", o.storageConditions),
            reorderLevel = get("reorder").toIntOrNull()?.takeIf { it != o.reorderLevel },
            barcode = s("barcode", o.barcode)
        )
    }

    fun hasChanges(o: Product, hasNewImage: Boolean): Boolean = hasNewImage || toUpdate(o) != ProductUpdateRequest()

    companion object {
        val Saver: Saver<ProductDraft, List<String>> = Saver(
            save = { d -> d.values.flatMap { listOf(it.key, it.value) } },
            restore = { l -> ProductDraft(l.chunked(2).associate { it[0] to it[1] }) }
        )

        fun from(p: Product) = ProductDraft(
            mapOf(
                "name" to p.name, "price" to p.price.fmt(), "generic" to p.genericName, "brand" to p.brandName,
                "manufacturer" to p.manufacturer, "category" to p.category, "dosage" to p.dosageForm, "strength" to p.strength,
                "pack" to p.packSize, "unit" to p.unit, "hsn" to p.hsnCode, "gst" to p.gstPercent.fmt(), "mrp" to p.mrp.fmt(),
                "purchase" to (p.purchasePrice?.fmt() ?: ""), "schedule" to p.scheduleType, "rx" to p.requiresPrescription.toString(),
                "storage" to p.storageConditions, "reorder" to p.reorderLevel.toString(), "barcode" to p.barcode
            )
        )

        private fun Double.fmt() = if (this % 1.0 == 0.0) toLong().toString() else toString()
    }
}

@Composable
fun rememberProductDraft(initial: Map<String, String> = mapOf("rx" to "false")): ProductDraft =
    rememberSaveable(saver = ProductDraft.Saver) { ProductDraft(initial) }

/** Picked image (cropped) + the camera/gallery sheet. Call [open] to show the sheet. */
class ProductImagePicker(val uri: Uri?, val open: () -> Unit)

@Composable
fun rememberProductImagePicker(): ProductImagePicker {
    val context = LocalContext.current
    var uri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var show by rememberSaveable { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val crop = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) r.data?.let { UCrop.getOutput(it) }?.let { uri = it }
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { u ->
        if (u != null) crop.launch(getUCropIntent(context, u))
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val u = cameraUri
        if (ok && u != null) crop.launch(getUCropIntent(context, u))
    }
    if (show) {
        ImageSourceSheet(
            onDismiss = { show = false },
            onCamera = { show = false; cameraUri = context.createImageUri().also { camera.launch(it) } },
            onGallery = { show = false; gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onRemove = if (uri != null) ({ show = false; uri = null }) else null,
            title = "Product image"
        )
    }
    return ProductImagePicker(uri) { show = true }
}

/** Full-screen add/edit form. [extra] is placed after the fields (e.g. opening stock on Add). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    title: String,
    saveText: String,
    draft: ProductDraft,
    enums: AppConfigEnums?,
    image: ProductImagePicker,
    existingImageUrl: String?,
    isLoading: Boolean,
    formError: String?,
    onSave: () -> Unit,
    onBack: () -> Unit,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    AppScreenScaffold(
        title = title,
        onBack = onBack,
        bottomBar = {
            com.saurabh.mediadminapp.ui.components.SaveBar(text = saveText, loading = isLoading, onClick = onSave)
        }
    ) { inner ->
        FormScaffold(Modifier.padding(inner)) {
            SectionHeader("Image")
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(120.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val model: Any? = image.uri ?: existingImageUrl?.takeIf { it.isNotBlank() }
                        if (model != null) Image(rememberAsyncImagePainter(model), "Product image", Modifier.size(120.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surface).padding(4.dp), contentScale = ContentScale.Fit)
                        else Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                }
                com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton(text = if (image.uri == null && existingImageUrl.isNullOrBlank()) "Add image" else "Change image", onClick = image.open, enabled = !isLoading, modifier = Modifier.padding(top = 8.dp))
            }

            SectionHeader("Basic details")
            Field(draft, "name", "Product name *", !isLoading, words = true)
            Field(draft, "category", "Category", !isLoading)
            Field(draft, "generic", "Generic name", !isLoading)
            Field(draft, "brand", "Brand name", !isLoading, words = true)
            Field(draft, "manufacturer", "Manufacturer", !isLoading, words = true)
            Field(draft, "barcode", "Barcode", !isLoading, KeyboardType.Ascii, caps = true)

            SectionHeader("Pricing")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(draft, "price", "Selling price (₹) *", !isLoading, KeyboardType.Decimal, Modifier.weight(1f))
                Field(draft, "mrp", "MRP (₹)", !isLoading, KeyboardType.Decimal, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(draft, "purchase", "Purchase price (₹)", !isLoading, KeyboardType.Decimal, Modifier.weight(1f))
                Field(draft, "gst", "GST %", !isLoading, KeyboardType.Decimal, Modifier.weight(1f))
            }
            Field(draft, "hsn", "HSN code", !isLoading, KeyboardType.Ascii, caps = true)

            SectionHeader("Packaging")
            Dropdown(draft, "dosage", "Dosage form", enums?.dosageForms.orEmpty(), !isLoading)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field(draft, "strength", "Strength", !isLoading, modifier = Modifier.weight(1f))
                Field(draft, "pack", "Pack size", !isLoading, modifier = Modifier.weight(1f))
            }
            Dropdown(draft, "unit", "Unit", enums?.units.orEmpty(), !isLoading)

            SectionHeader("Compliance & inventory")
            Dropdown(draft, "schedule", "Schedule", enums?.scheduleTypes?.takeIf { it.isNotEmpty() } ?: FALLBACK_SCHEDULES, !isLoading) { sel ->
                // Schedule H and above need a prescription; OTC does not. Still editable below.
                draft["rx"] = (sel != "OTC").toString()
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Requires prescription", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = draft["rx"] == "true", onCheckedChange = { draft["rx"] = it.toString() }, enabled = !isLoading)
            }
            Field(draft, "storage", "Storage conditions", !isLoading)
            Field(draft, "reorder", "Reorder level", !isLoading, KeyboardType.Number)

            extra()
            formError?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun Field(
    draft: ProductDraft, key: String, label: String, enabled: Boolean,
    keyboard: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier.fillMaxWidth(),
    caps: Boolean = false, words: Boolean = false,
) {
    GsTextField(
        value = draft[key], onValueChange = { draft[key] = it },
        label = { Text(label, maxLines = 1) }, singleLine = true, enabled = enabled,
        isError = draft.errors[key] != null, supportingText = draft.errors[key]?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboard,
            capitalization = when { caps -> androidx.compose.ui.text.input.KeyboardCapitalization.Characters; words -> androidx.compose.ui.text.input.KeyboardCapitalization.Words; else -> androidx.compose.ui.text.input.KeyboardCapitalization.Sentences },
            autoCorrectEnabled = !caps,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier
    )
}

/** Dropdown over server-provided values; a plain text field when the list is unknown. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(
    draft: ProductDraft, key: String, label: String, options: List<String>, enabled: Boolean,
    onSelected: (String) -> Unit = {},
) {
    if (options.isEmpty()) { Field(draft, key, label, enabled); return }
    var expanded by remember { mutableStateOf(false) }
    // A saved value outside the list (older data) stays selectable.
    val all = if (draft[key].isNotBlank() && draft[key] !in options) options + draft[key] else options
    ExposedDropdownMenuBox(expanded = expanded && enabled, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
        GsTextField(
            value = draft[key], onValueChange = {}, readOnly = true, enabled = enabled,
            label = { Text(label, maxLines = 1) }, singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            isError = draft.errors[key] != null, supportingText = draft.errors[key]?.let { { Text(it) } },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
            all.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { draft[key] = opt; onSelected(opt); expanded = false })
            }
        }
    }
}
