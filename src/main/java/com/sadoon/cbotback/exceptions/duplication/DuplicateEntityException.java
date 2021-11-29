package com.sadoon.cbotback.exceptions.duplication;

import com.sadoon.cbotback.exceptions.ApiError;
import com.sadoon.cbotback.exceptions.CustomException;

public class DuplicateEntityException extends CustomException {

    protected DuplicateEntityException(String entity, String option) {
        super(String.format("A %s already exists %s", entity, option));
    }

    protected DuplicateEntityException(String entity, String option, ApiError subError) {
        super(String.format("A %s already exists %s", entity, option), subError);
    }
}
