package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;

import com.example.myapplication.network.dto.VendorResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public final class VendorDetailsDialog {

    private VendorDetailsDialog() {
    }

    public static void show(Context context, VendorResponse vendor) {
        if (vendor == null) {
            return;
        }

        List<String> lines = new ArrayList<>();
        if (!TextUtils.isEmpty(vendor.address)) {
            lines.add("Address: " + vendor.address);
        }
        if (!TextUtils.isEmpty(vendor.website)) {
            lines.add("Website: " + vendor.website);
        }
        if (!TextUtils.isEmpty(vendor.email)) {
            lines.add("Email: " + vendor.email);
        }
        if (!TextUtils.isEmpty(vendor.phone)) {
            lines.add("Phone: " + vendor.phone);
        }

        String message = lines.isEmpty()
                ? "No contact details are available for this vendor yet."
                : TextUtils.join("\n\n", lines);

        new MaterialAlertDialogBuilder(context)
                .setTitle(vendor.name != null ? vendor.name : "Vendor")
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
}
