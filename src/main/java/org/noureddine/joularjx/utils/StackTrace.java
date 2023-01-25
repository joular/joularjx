package org.noureddine.joularjx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTrace {

    private List<StackTraceElement> stackTrace;

    public StackTrace() {
        this.stackTrace = new ArrayList<>();
    }

    public StackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = Arrays.asList(stackTrace);
    }

    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = Arrays.asList(stackTrace);
    }

    public List<StackTraceElement> getStackTrace() {
        return this.stackTrace;
    }

    @Override
    public String toString() {
        String res = "";

        /*Appening elements to res String in reverse order. The least recent element (the bottom of the stack trace) will be written first, and the most recent one last.*/
        for(int i = this.stackTrace.size()-1; i >= 0; i--){
            StackTraceElement element = this.stackTrace.get(i);
            res += element.getClassName()+"."+element.getMethodName()+";";
        }

        //Removing the last ";"
        return res.substring(0, res.length()-1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stackTrace == null) ? 0 : stackTrace.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
            
        StackTrace other = (StackTrace) obj;
        if (stackTrace == null) {
            if (other.stackTrace != null)
                return false;
        } else if (!stackTrace.equals(other.stackTrace))
            return false;
        return true;
    }
    
}
