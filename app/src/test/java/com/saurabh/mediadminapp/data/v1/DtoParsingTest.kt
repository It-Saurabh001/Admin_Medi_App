package com.saurabh.mediadminapp.data.v1

import com.google.gson.reflect.TypeToken
import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.core.parseApiError
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.data.v1.dto.AppConfig
import com.saurabh.mediadminapp.data.v1.dto.AuditLog
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.DashboardSummary
import com.saurabh.mediadminapp.data.v1.dto.Invoice
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.data.v1.dto.Sale
import com.saurabh.mediadminapp.data.v1.dto.SalesReportSummary
import com.saurabh.mediadminapp.data.v1.dto.StockOperationResult
import com.saurabh.mediadminapp.data.v1.dto.Supplier
import com.saurabh.mediadminapp.data.v1.dto.TokenData
import com.saurabh.mediadminapp.data.v1.dto.User
import com.saurabh.mediadminapp.data.v1.dto.WriteOffResult
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoParsingTest {

    private inline fun <reified T> env(json: String): T? =
        V1Gson.instance.fromJson<ApiEnvelope<T>>(json, object : TypeToken<ApiEnvelope<T>>() {}.type).data

    private inline fun <reified T> paged(json: String): PagedEnvelope<T> =
        V1Gson.instance.fromJson(json, object : TypeToken<PagedEnvelope<T>>() {}.type)

    private val orderJson = """{"cancel_reason":"","category":"Analgesic","created_at":"2026-11-14T17:33:50Z","doctor_name":"","image_url":null,"message":"Need by evening","order_id":"ORD_f828d7b2e6d6","patient_name":"Priya Sharma","prescription_url":null,"prescription_verified":false,"price":31.0,"product_id":"PROD_c7a36281e0","product_name":"Paracetamol 500mg","quantity":2,"requires_prescription":false,"status":"sold","total_amount":62.0,"updated_at":"2026-11-14T17:33:51Z","user_id":"UID_06ade2a9b763","user_name":"Priya S"}"""
    private val saleJson = """{"batch_id":"BAT_2cceba2ef4f8","batch_no":"B-2026-01","gst_percent":12.0,"image_url":null,"order_id":"ORD_f828d7b2e6d6","order_status":"returned","price":31.0,"product_id":"PROD_c7a36281e0","product_name":"Paracetamol 500mg","quantity":2,"remaining_stock":103,"sale_id":"SELL_dbd3c87884db","sold_at":"2026-11-14T17:33:51Z","sold_by":"AID_root","total_amount":62.0,"user_id":"UID_06ade2a9b763","user_name":"Priya S"}"""
    private val batchJson = """{"batch_id":"BAT_2cceba2ef4f8","batch_no":"B-2026-01","created_at":"2026-11-14T17:33:50Z","days_to_expiry":594,"expiry_date":"2028-06-30","is_expired":false,"is_legacy_placeholder":false,"mfg_date":"2026-01-10","mrp":32.0,"product_id":"PROD_c7a36281e0","product_name":"Paracetamol 500mg","purchase_price":22.0,"quantity":5,"received_date":"2026-11-14","supplier_id":null,"supplier_name":"","updated_at":"2026-11-14T17:33:50Z"}"""
    private val productJson = """{"barcode":"8901234567890","brand_name":"Calpol","category":"Analgesic","created_at":"2026-11-14T17:33:50Z","dosage_form":"Tablet","generic_name":"Paracetamol","gst_percent":12.0,"hsn_code":"3004","image_url":null,"in_stock":false,"is_active":true,"manufacturer":"GSK","mrp":32.0,"name":"Paracetamol 500mg","pack_size":"15 tablets","price":30.0,"product_id":"PROD_c7a36281e0","purchase_price":null,"reorder_level":10,"requires_prescription":false,"schedule_type":"OTC","sellable_stock":0,"stock":0,"storage_conditions":"","strength":"500mg","unit":"strip","updated_at":"2026-11-14T17:33:50Z"}"""

    @Test fun auth_tokens() {
        val t = env<TokenData>("""{"data":{"access_token":"a.b.c","account_id":"UID_06ade2a9b763","account_type":"user","expires_in":3600,"refresh_token":"d.e.f","role":"user","token_type":"Bearer"},"message":"Login successful","status":200}""")!!
        assertEquals("a.b.c", t.accessToken); assertEquals("d.e.f", t.refreshToken)
        assertEquals(3600, t.expiresIn); assertEquals("UID_06ade2a9b763", t.accountId)
    }

    @Test fun user_and_admin() {
        val u = env<User>("""{"data":{"address":"12 MG Road, Pune","created_at":"2026-11-14T17:33:47Z","email":"priya@example.com","image_url":null,"is_approved":false,"is_blocked":false,"is_deleted":false,"name":"Priya Sharma","phone_number":"+919811111111","pin_code":"411001","role":"user","user_id":"UID_06ade2a9b763"},"message":"ok","status":201}""")!!
        assertEquals("UID_06ade2a9b763", u.userId); assertEquals(false, u.isApproved)
        assertNotNull(u.createdAt); assertEquals("411001", u.pinCode)
        val a = env<Admin>("""{"data":{"admin_id":"AID_68e297784b96","created_at":"2026-11-14T17:33:46Z","email":"asha@pharmacy.in","image_url":null,"is_active":true,"name":"Asha Admin","phone_number":null,"role":"admin"},"message":"ok","status":200}""")!!
        assertEquals("AID_68e297784b96", a.adminId); assertNull(a.phoneNumber); assertTrue(a.isActive)
    }

    @Test fun product_batch_movement() {
        val p = env<Product>("""{"data":$productJson,"message":"ok","status":200}""")!!
        assertEquals("PROD_c7a36281e0", p.productId); assertEquals(30.0, p.price, 0.0)
        assertNull(p.purchasePrice); assertEquals("OTC", p.scheduleType); assertNotNull(p.updatedAt)
        val b = env<Batch>("""{"data":$batchJson,"message":"ok","status":200}""")!!
        assertEquals("BAT_2cceba2ef4f8", b.batchId); assertEquals("2028-06-30", b.expiryDate.toString()); assertEquals(594, b.daysToExpiry)
        val op = env<StockOperationResult>("""{"data":{"batch":$batchJson,"batch_created":true,"movement":{"batch_id":"BAT_2cceba2ef4f8","change_qty":100,"created_at":"2026-11-14T17:33:50Z","movement_id":"MOV_916c55c85fe34f5e","performed_by":"AID_root","product_id":"PROD_c7a36281e0","product_stock":100,"reason":"PURCHASE","reference_id":"INV-778"},"product_stock":100},"message":"ok","status":201}""")!!
        assertEquals(true, op.batchCreated); assertEquals(100, op.productStock)
        assertEquals("PURCHASE", op.movement!!.reason); assertEquals(100, op.movement!!.changeQty)
        val w = env<WriteOffResult>("""{"data":{"batch":$batchJson,"movement":{"batch_id":"BAT_2cceba2ef4f8","change_qty":-5,"created_at":"2026-11-14T17:33:51Z","movement_id":"MOV_fff22ab775294112","performed_by":"AID_root","product_id":"PROD_c7a36281e0","product_stock":100,"reason":"EXPIRED_WRITEOFF","reference_id":"WOFF_89604c5ccb"}},"message":"ok","status":200}""")!!
        assertEquals(-5, w.movement.changeQty)
    }

    @Test fun order_with_sales_and_sale() {
        val o = env<Order>("""{"data":${orderJson.dropLast(1)},"sales":[$saleJson]},"message":"Order sold","status":200}""")!!
        assertEquals(OrderStatus.SOLD, o.status); assertEquals("ORD_f828d7b2e6d6", o.orderId)
        assertEquals(62.0, o.totalAmount, 0.0); assertEquals(1, o.sales!!.size)
        assertEquals("SELL_dbd3c87884db", o.sales!![0].saleId); assertEquals(OrderStatus.RETURNED, o.sales!![0].orderStatus)
        assertNull(o.restocked)
        val s = V1Gson.instance.fromJson(saleJson, Sale::class.java)
        assertEquals(103, s.remainingStock); assertNotNull(s.soldAt)
    }

    @Test fun invoice() {
        val inv = env<Invoice>("""{"data":{"currency":"INR","generated_at":"2026-11-14T17:33:51Z","grand_total":62.0,"gst_total":6.64,"invoice_number":"INV-ORD_f828d7b2e6d6","issued_at":"2026-11-14T17:33:51Z","items":[{"batch_id":"BAT_2cceba2ef4f8","batch_no":"B-2026-01","expiry_date":"2028-06-30","gst_amount":6.64,"gst_percent":12.0,"line_total":62.0,"product_id":"PROD_c7a36281e0","product_name":"Paracetamol 500mg","quantity":2,"taxable_amount":55.36,"unit_price":31.0}],"order":$orderJson,"prices_include_gst":true,"subtotal":55.36,"user":{"address":"14 MG Road, Pune","email":"priya@example.com","name":"Priya S","phone_number":"+919811111111","pin_code":"411002","user_id":"UID_06ade2a9b763"}},"message":"Invoice generated","status":200}""")!!
        assertEquals("INV-ORD_f828d7b2e6d6", inv.invoiceNumber); assertEquals(62.0, inv.grandTotal, 0.0)
        assertEquals(1, inv.items.size); assertEquals(31.0, inv.items[0].unitPrice, 0.0)
        assertEquals(OrderStatus.SOLD, inv.order.status); assertEquals("UID_06ade2a9b763", inv.user.userId)
    }

    @Test fun supplier_dashboard_audit() {
        val s = env<Supplier>("""{"data":{"address":"Bhosari MIDC, Pune","contact_phone":"+912012345678","created_at":"2026-11-14T17:33:50Z","email":"orders@medsupply.in","gst_number":"27ABCDE1234F1Z5","is_active":true,"name":"MedSupply Pvt Ltd","supplier_id":"SUP_0debfd314e","updated_at":"2026-11-14T17:33:50Z"},"message":"ok","status":200}""")!!
        assertEquals("SUP_0debfd314e", s.supplierId); assertEquals("27ABCDE1234F1Z5", s.gstNumber)
        val d = env<DashboardSummary>("""{"data":{"approved_orders":1,"date":"2026-11-14","expired_batches":0,"low_stock":0,"near_expiry_batches":0,"near_expiry_days":90,"out_of_stock":0,"pending_orders":0,"products":2,"today_sales_count":0,"today_sales_total":0.0},"message":"ok","status":200}""")!!
        assertEquals(2, d.products); assertEquals("2026-11-14", d.date.toString()); assertEquals(90, d.nearExpiryDays)
        val p = paged<AuditLog>("""{"data":[{"action":"supplier.deactivate","actor_id":"AID_root","actor_role":"admin","audit_id":"AUD_40d60e0ebf824d93","created_at":"2026-11-14T17:33:51Z","details":{"is_active":false},"entity":"supplier","entity_id":"SUP_0debfd314e"}],"message":"ok","pagination":{"limit":2,"offset":0,"total":40},"status":200}""")
        assertEquals("AUD_40d60e0ebf824d93", p.data[0].auditId); assertNotNull(p.data[0].details)
        assertEquals(40, p.pagination.total)
    }

    @Test fun sales_report_summary_and_paged_list() {
        val p = paged<Sale>("""{"data":[$saleJson],"message":"ok","pagination":{"limit":20,"offset":0,"total":1},"status":200,"summary":{"from":"2026-10-15","group_by":"day","orders":0,"quantity":0,"revenue":0.0,"to":"2026-11-14"}}""")
        assertEquals(1, p.data.size); assertEquals(20, p.pagination.limit); assertNotNull(p.summary)
        val sum = V1Gson.instance.fromJson(p.summary, SalesReportSummary::class.java)
        assertEquals("day", sum.groupBy); assertEquals("2026-11-14", sum.to.toString())
    }

    @Test fun app_config() {
        val c = env<AppConfig>("""{"data":{"api_prefix":"/api/v1","api_version":"v1","docs_enabled":true,"enums":{"account_types":["user","admin"],"device_platforms":["android","ios"],"dosage_forms":["Tablet"],"order_statuses":["pending","approved","sold","cancelled","returned"],"prescription_schedules":["H"],"schedule_types":["OTC","H"],"stock_movement_reasons":["PURCHASE"],"units":["strip"]},"error_codes":["VALIDATION_ERROR"],"min_app_version":null,"otp":{"expires_in":300,"length":6,"max_attempts":5},"pagination":{"default_limit":20,"max_limit":100},"server_time":"2026-11-14T17:33:45Z","uploads":{"allowed_mime_types":["image/png"],"image_mime_types":["image/png"],"max_request_bytes":6291456,"max_upload_mb":5}},"message":"App configuration","status":200}""")!!
        assertEquals("v1", c.apiVersion); assertNull(c.minAppVersion); assertNotNull(c.serverTime)
        assertEquals(5, c.uploads.maxUploadMb); assertEquals(6291456L, c.uploads.maxRequestBytes)
        assertEquals(5, c.enums.orderStatuses.size); assertEquals(100, c.pagination.maxLimit)
    }

    @Test fun error_with_fields() {
        val e = parseApiError(400, """{"code":"VALIDATION_ERROR","fields":{"email":"must be a valid email address","password":"must be at least 8 characters"},"message":"Validation failed","status":400}""")
        assertEquals(ErrorCode.VALIDATION_ERROR, e.code); assertEquals(2, e.fields!!.size)
        assertEquals("Validation failed", e.message)
    }
}
