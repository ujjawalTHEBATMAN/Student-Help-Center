package com.example.abcd.AuthenTication.regestration.cloudinaryUtils;

import com.cloudinary.android.signed.Signature;
import com.cloudinary.android.signed.SignatureProvider;
import java.util.Map;

public class CloudinaryConfig implements SignatureProvider {
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final boolean unsigned;

    public CloudinaryConfig(String cloudName, String apiKey, String apiSecret, boolean unsigned) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.unsigned = unsigned;
    }

    @Override
    public Signature provideSignature(Map options) {
        long timestamp = System.currentTimeMillis() / 1000L;
        return new Signature(apiKey, apiSecret, timestamp);
    }

    @Override
    public String getName() {
        return "Custom_Cloudinary_Config";
    }
}
