package org.noureddine.joularjx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CallTree {

    private List<StackTraceElement> callTree;

    public CallTree() {
        this.callTree = new ArrayList<>();
    }

    public CallTree(StackTraceElement[] stackTrace) {
        this.callTree = Arrays.asList(stackTrace);
    }

    public CallTree(List<StackTraceElement> stackTrace) {
        this.callTree = stackTrace;
    }

    public void setCallTree(StackTraceElement[] stackTrace) {
        this.callTree = Arrays.asList(stackTrace);
    }

    public List<StackTraceElement> getCallTree() {
        return this.callTree;
    }

    @Override
    public String toString() {
        String res = "";

        /*Appening elements to res String in reverse order. The least recent element (the bottom of the stack trace) will be written first, and the most recent one last.*/
        for(int i = this.callTree.size()-1; i >= 0; i--){
            StackTraceElement element = this.callTree.get(i);
            res += element.getClassName()+"."+element.getMethodName()+";";
        }

        //Removing the last ";"
        return res.substring(0, res.length()-1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((callTree == null) ? 0 : callTree.hashCode());
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
            
        CallTree other = (CallTree) obj;
        if (callTree == null) {
            if (other.callTree != null)
                return false;
        } else if (!callTree.equals(other.callTree))
            return false;
        return true;
    }
    
}
