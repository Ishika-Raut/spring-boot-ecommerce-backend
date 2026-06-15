package com.ecommerce.backend.enums;


public enum AttributeType 
{
    TEXT,           // e.g., Brand, Model
    NUMBER,         // e.g., RAM (8), Storage (128)
    SELECT,         // e.g., Color (Red, Blue), Size (S, M, L)
    BOOLEAN,        // e.g., Waterproof
    MULTI_SELECT,   // e.g., Features
    DATE
}
