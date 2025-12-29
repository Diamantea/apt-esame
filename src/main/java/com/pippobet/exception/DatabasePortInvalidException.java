package com.pippobet.exception;

public class DatabasePortInvalidException extends Exception
{
    private static final String MESSAGE = "Invalid DB port";
    public DatabasePortInvalidException(NumberFormatException e)
    {
        super(MESSAGE, e);
    }
}
