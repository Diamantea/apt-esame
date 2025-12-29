package com.pippobet.exception;

public class EmptyInputParameterException extends Exception
{
    private static final String MSG = "Param %s is specified but empty";


    public EmptyInputParameterException(String dbHostInputParam)
    {
        super(String.format(MSG, dbHostInputParam));
    }
}
