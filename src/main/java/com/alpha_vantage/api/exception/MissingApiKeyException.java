package com.alpha_vantage.api.exception;

public class MissingApiKeyException extends Exception {
    public MissingApiKeyException(){
        super("the parameter apikey is invalid or missing. Please claim your free API key on (https://www.alphavantage.co/support/#api-key). It should take less than 20 seconds, and is free permanently.");
    }
}