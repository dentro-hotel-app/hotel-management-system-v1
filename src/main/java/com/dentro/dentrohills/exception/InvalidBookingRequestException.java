package com.dentro.dentrohills.exception;

import org.apache.logging.log4j.message.Message;

public class InvalidBookingRequestException extends RuntimeException{
    public InvalidBookingRequestException(String message){
        super(message);
    }
}
