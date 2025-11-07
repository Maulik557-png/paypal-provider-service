package com.hulkhiretech.payments.util;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.hulkhiretech.payments.paypal.res.error.PaypalErrorDetail;
import com.hulkhiretech.payments.paypal.res.error.PaypalErrorResponse;

public class PaypalErrorUtil {

    private PaypalErrorUtil() {
        // Prevent instantiation
    }
    
    /**
	 * Generates a summary string from the given PayPal error response.
	 *
	 * @param paypalErrorRes the PayPal error response
	 * @return a summary string of the error
	 */
    public static String getResponseSummary(PaypalErrorResponse paypalErrorRes) {
        if (paypalErrorRes == null) {
            return "Unknown error occurred while processing PayPal response.";
        }

        StringBuilder summary = new StringBuilder();

        // Handle Identity/OAuth style errors first
        if (isIdentityError(paypalErrorRes)) {
            appendIdentityError(summary, paypalErrorRes);
        } 
        // Handle REST style errors
        else {
            appendRestError(summary, paypalErrorRes);
        }

        if (summary.isEmpty()) {
            summary.append("PayPal returned an error but no details were provided.");
        }

        return summary.toString().trim();
    }

    // ---------------- Helper Methods ----------------

    private static boolean isIdentityError(PaypalErrorResponse res) {
        return StringUtils.hasText(res.getError()) || StringUtils.hasText(res.getErrorDescription());
    }

    private static void appendIdentityError(StringBuilder summary, PaypalErrorResponse res) {
        if (StringUtils.hasText(res.getError())) {
            summary.append(res.getError());
        }
        if (StringUtils.hasText(res.getErrorDescription())) {
            if (!summary.isEmpty()) summary.append(" - ");
            summary.append(res.getErrorDescription());
        }
    }

    private static void appendRestError(StringBuilder summary, PaypalErrorResponse res) {
        if (StringUtils.hasText(res.getName())) {
            summary.append(res.getName());
        }

        if (StringUtils.hasText(res.getMessage())) {
            if (!summary.isEmpty()) summary.append(" - ");
            summary.append(res.getMessage());
        }

        if (!CollectionUtils.isEmpty(res.getDetails())) {
            PaypalErrorDetail detail = res.getDetails().get(0);
            appendErrorDetail(summary, detail);
        }
    }

    private static void appendErrorDetail(StringBuilder summary, PaypalErrorDetail detail) {
        if (StringUtils.hasText(detail.getField())) {
            summary.append(" | Field: ").append(detail.getField());
        }

        if (StringUtils.hasText(detail.getDescription())) {
            summary.append(" - ").append(detail.getDescription());
        }

        if (StringUtils.hasText(detail.getIssue())) {
            summary.append(" (Issue: ").append(detail.getIssue()).append(")");
        }
    }
}