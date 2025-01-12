package com.developerali.aima.Helpers;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.developerali.aima.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    public static SpannableString applySpannable(Activity activity, String text, final String fullCaption, TextView textView) {
        SpannableString spannableString = new SpannableString(text);
        int maxLength = 150;

        // Example regex for links, hashtags, and "Read more" / "Read less"
        Pattern pattern = Pattern.compile("(#[a-zA-Z0-9_]+|https?://\\S+|Read more|Read less)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String match = matcher.group();
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (match.equals("Read more")) {
                        // Switch to full caption and show "Read less"
                        SpannableString fullSpannableString = applySpannable(activity, fullCaption+"... Read less", fullCaption, textView);
                        textView.setText(fullSpannableString);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        textView.setHighlightColor(Color.TRANSPARENT);
                    } else if (match.equals("Read less")) {
                        // Switch back to truncated text and show "Read more"
                        String truncatedText = fullCaption.substring(0, maxLength) + "... Read more";
                        SpannableString truncatedSpannableString = applySpannable(activity, truncatedText, fullCaption, textView);
                        textView.setText(truncatedSpannableString);
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                        textView.setHighlightColor(Color.TRANSPARENT);
                    } else if (match.startsWith("http") || match.startsWith("www")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(match));
                        activity.startActivity(browserIntent);
                    } else {
                        // Handle hashtag/tag clicks
                        Toast.makeText(widget.getContext(), "Clicked on tag: " + match, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(activity.getColor(R.color.blue_purple)); // Set link color
                    ds.setUnderlineText(false); // Remove underline
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Enable text copying on long press (only once, outside the while loop)
        textView.setOnLongClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                // Copy the text to clipboard
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Caption", textView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "Text copied!", Toast.LENGTH_SHORT).show();
                return true; // Long press event handled
            }
            return false; // Default long press behavior
        });

        return spannableString;
    }


    public static String formatVideoViews(int views) {
        if (views >= 1_000_000) {
            return String.format("%.1fM", views / 1_000_000.0); // Format as "1.2M"
        } else if (views >= 1_000) {
            return String.format("%.1fK", views / 1_000.0); // Format as "1.2K"
        } else {
            return String.valueOf(views); // No formatting for numbers below 1,000
        }
    }

    public static void startCounter(int countTill, TextView textView, String prefix, String suffix){
        ValueAnimator animator = ValueAnimator.ofInt(0, countTill);
        animator.setDuration(3000); // Animation duration in milliseconds
        animator.addUpdateListener(valueAnimator -> {
            int animatedValue = (int) valueAnimator.getAnimatedValue();
            textView.setText(prefix + animatedValue + suffix);
        });
        animator.start();
    }

    public static SpannableString applySpannable(String text) {
        SpannableString spannableString = new SpannableString(text);

        // #tag highlighting
        Pattern hashtagPattern = Pattern.compile("#\\w+");
        Matcher hashtagMatcher = hashtagPattern.matcher(text);
        while (hashtagMatcher.find()) {
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),
                    hashtagMatcher.start(), hashtagMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Link highlighting
        Pattern urlPattern = Patterns.WEB_URL;
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()) {
            spannableString.setSpan(new URLSpan(text.substring(urlMatcher.start(), urlMatcher.end())),
                    urlMatcher.start(), urlMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    public static List<String> extractLinks(String text) {
        // Define a regex pattern for URLs
        String urlPattern = "(https?://[\\w.-]+(?:\\.[a-z]{2,4})?(?:/\\S*)?)";

        // Compile the pattern
        Pattern pattern = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);

        // Match the pattern in the text
        Matcher matcher = pattern.matcher(text);

        // Store the links in a list
        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group());
        }

        return links;
    }
}
