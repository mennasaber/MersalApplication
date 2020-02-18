package com.example.chatapp.Models;

public class Block {
    String blockId ;
    String blockerNumber ;

    public Block() {}

    public Block(String blockId, String blockerNumber) {
        this.blockId = blockId;
        this.blockerNumber = blockerNumber;
    }

    public String getBlockerNumber() {
        return blockerNumber;
    }

    public void setBlockerNumber(String blockerNumber) {
        this.blockerNumber = blockerNumber;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }
}
