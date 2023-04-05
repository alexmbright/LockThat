package dev.alexbright.lockthat.enums;

public enum LockType {
    BARREL("barrel"),
    BLAST_FURNACE("blast furnace"),
    BREWING_STAND("brewing stand"),
    CHEST("chest"),
    CONTAINER("container"),
    DISPENSER("dispenser"),
    DOOR("door"),
    DOUBLE_CHEST("double chest"),
    DROPPER("dropper"),
    FURNACE("furnace"),
    GATE("gate"),
    HOPPER("hopper"),
    OPENABLE("openable"),
    SHULKER_BOX("shulker box"),
    SMOKER("smoker"),
    TRAPDOOR("trapdoor");

    private final String name;

    LockType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() { return getName(); }
}
