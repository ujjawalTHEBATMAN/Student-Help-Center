package com.example.abcd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;

public class PDFWebViewActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;
    private String pdfUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_webview);

        pdfUrl = getIntent().getStringExtra("PDF_URL");
        String pdfName = getIntent().getStringExtra("PDF_NAME");
        
        if (pdfUrl == null || pdfUrl.isEmpty()) {
            Toast.makeText(this, "Invalid PDF URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the title to the PDF name if available
        if (pdfName != null && !pdfName.isEmpty()) {
            setTitle(pdfName);
        }

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        // Configure WebView settings
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PDFWebViewActivity.this, "Error loading PDF: " + description, Toast.LENGTH_SHORT).show();
            }
        });

        // Encode the URL to handle special characters
        String encodedPdfUrl = Uri.encode(pdfUrl, ":/%#?&=@");
        
        // Use Google Docs Viewer with mobile optimization
        String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + encodedPdfUrl;
        webView.loadUrl(googleDocsUrl);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
} 