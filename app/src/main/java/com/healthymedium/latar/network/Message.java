package com.healthymedium.latar.network;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Message {

    public static final char BEGIN = 0x01; // Start of Heading
    public static final char END = 0x04; // End of Transmission

    byte body[];
    String comment;
    char command;
    char control;
    boolean valid;

    public Message() {
        comment = new String();
        body = new byte[0];
        control = Constants.bel;
    }

    public Message(char cmd) {
        command = cmd;
        comment = new String();
        body = new byte[0];
        control = Constants.bel;
    }

    public Message(byte buffer[]){
        comment = new String();
        body = new byte[0];
        control = Constants.bel;
        parseBuffer(buffer);
    }

    boolean parseBuffer(byte buffer[]){
        valid = false;
        int size = buffer.length;
        if(size < 2){
            return false;
        }
        if(buffer[0] != BEGIN || buffer[size-1] != END){
            return false;
        }
        buffer = Arrays.copyOfRange(buffer, 1, size-1);
        buffer = parseHeader(buffer);
        buffer = parseComment(buffer);
        body = buffer;
        valid  = true;
        return true;
    }

    public boolean isValid(){
        return valid;
    }

    public  boolean isEnquiry(){
        return (control== Constants.enq);
    }

    public void setEnquiry(){
        control = Constants.enq;
    }

    public boolean isAcknowledgementValid(){
        return (control== Constants.ack || control== Constants.nak);
    }

    public boolean wasAcknowledged(){
        return (control== Constants.ack);
    }

    public void setAcknowledgement(boolean error){
        control = (error)? Constants.nak: Constants.ack;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public String getComment(){
        return comment;
    }

    public byte[] toBuffer(){
        int size = 1; // begin

        int headerIndex = size;
        byte header[] = writeHeader();
        size += header.length;

        int bodyIndex = size;
        size += body.length;

        int commentsIndex = size;
        byte comments[] = comment.getBytes();
        if(comments.length !=0){
            size += comments.length;
            size += 2;
        }

        size += 1; // end

        byte buffer[] = new byte[size];
        buffer[0] = BEGIN;
        System.arraycopy(header,0,buffer,1,header.length);
        System.arraycopy(body,0,buffer,bodyIndex,body.length);
        if(comments.length !=0){
            buffer[commentsIndex] = Constants.stx;
            System.arraycopy(comments,0,buffer,commentsIndex+1,comments.length);
            buffer[commentsIndex+comments.length+1] = Constants.etx;
        }
        buffer[size-1] = END;

        return buffer;
    }

    public char getCommand(){
        return command;
    }

    public void setCommand(char cmd){
        command = cmd;
    }

    public void setBody(byte buffer[]){
        body = buffer.clone();
    }

    public void setBody(String string){
        body = string.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getBody(){
        return body.clone();
    }

    public String getBodyAsString(){
        return new String(body, StandardCharsets.UTF_8);
    }

    byte[] parseHeader(byte buffer[]){
        command = (char) buffer[0];
        control = (char) buffer[1];
        return Arrays.copyOfRange(buffer, 2, buffer.length);
    }

    byte[] parseComment(byte buffer[]){
        byte slice[] = buffer.clone();
        int last = buffer.length-1;
        if(last<0){
            return slice;
        }
        if(buffer[last]== Constants.etx){
            int first = -1;
            for(int i=0;i<last;i++) {
                if(buffer[i]== Constants.stx){
                    first = i;
                    break;
                }
            }
            if(first != -1){
                slice = Arrays.copyOfRange(buffer, 0, first);
                byte commentArray[] = Arrays.copyOfRange(buffer, first, last);
                comment = new String(commentArray);
            }
        }
        return slice;
    }

    byte[] writeHeader(){
        byte[] buffer = new byte[2];
        buffer[0] = (byte) command;
        buffer[1] = (byte) control;
        return buffer;
    }


    static class Constants {
        static final char stx = 0x02; // Start of Text
        static final char etx = 0x03; // End of Text
        static final char enq = 0x05; // Enquiry
        static final char ack = 0x06; // Acknowledge
        static final char nak = 0x15; // Negative Acknowledge
        static final char bel = 0x07; // Bell, Alert
    }



}
