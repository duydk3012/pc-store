package com.datn.pc_store.modules.order.util;

public final class OrderAddressHelper {
    private OrderAddressHelper() {}

    public record DecodedAddress(String recipientName, String recipientPhone, String note, String address) {}

    public static String encodeAddress(String address, String recipientName, String recipientPhone, String note) {
        String cleanAddr = address != null ? address.trim() : "";
        String rec = recipientName != null ? recipientName.trim() : "";
        String tel = recipientPhone != null ? recipientPhone.trim() : "";
        String n = note != null ? note.trim().replace("\r", " ").replace("\n", " ") : "";

        if (rec.isEmpty() && tel.isEmpty() && n.isEmpty()) {
            return cleanAddr.length() > 500 ? cleanAddr.substring(0, 500) : cleanAddr;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[REC:").append(rec)
          .append("|TEL:").append(tel)
          .append("|NOTE:").append(n)
          .append("] ")
          .append(cleanAddr);

        String result = sb.toString();
        if (result.length() > 500) {
            return result.substring(0, 500);
        }
        return result;
    }

    public static DecodedAddress decodeAddress(String raw) {
        if (raw == null || raw.isBlank()) {
            return new DecodedAddress(null, null, null, "");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[REC:") && trimmed.contains("]")) {
            int endIdx = trimmed.indexOf(']');
            String meta = trimmed.substring(5, endIdx); // after "[REC:"
            String clean = trimmed.substring(endIdx + 1).trim();

            String rec = null;
            String tel = null;
            String note = null;

            String[] parts = meta.split("\\|", 3);
            if (parts.length > 0 && !parts[0].isBlank()) {
                rec = parts[0].trim();
            }
            if (parts.length > 1 && parts[1].startsWith("TEL:")) {
                String val = parts[1].substring(4).trim();
                if (!val.isBlank()) tel = val;
            }
            if (parts.length > 2 && parts[2].startsWith("NOTE:")) {
                String val = parts[2].substring(5).trim();
                if (!val.isBlank()) note = val;
            }
            return new DecodedAddress(rec, tel, note, clean);
        }
        return new DecodedAddress(null, null, null, trimmed);
    }
}

