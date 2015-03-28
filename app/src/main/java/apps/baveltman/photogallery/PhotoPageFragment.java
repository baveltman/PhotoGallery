package apps.baveltman.photogallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment to control webview of single grid images
 */
public class PhotoPageFragment extends VisibleFragment {
    private String mUrl;
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mUrl = getActivity().getIntent().getData().toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page, parent, false);

        final ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.photo_progressBar);
        progressBar.setMax(100); // WebChromeClient reports in range 0-100
        final TextView titleTextView = (TextView)v.findViewById(R.id.photo_textView);

        mWebView = (WebView)v.findViewById(R.id.photo_webview);

        //enable js for webview
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String s){
                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress){
                    if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

            public void onReceivedTitle(WebView webView, String title) {
                    titleTextView.setText(title);
                }
            });

        mWebView.loadUrl(mUrl);

        return v;
    }
}
