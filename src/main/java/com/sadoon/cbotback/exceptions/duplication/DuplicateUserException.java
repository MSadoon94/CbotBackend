package com.sadoon.cbotback.exceptions.duplication;

import com.sadoon.cbotback.exceptions.ApiError;

public class DuplicateUserException extends DuplicateEntityException{
    public DuplicateUserException(String entity, String option) {
        super(entity, option);
    }

    public DuplicateUserException(String entity, String option, ApiError subError) {
        super(entity, option, subError);
    }
}
