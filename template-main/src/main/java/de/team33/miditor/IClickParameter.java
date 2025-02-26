package de.team33.miditor;

public interface IClickParameter {
    long getMin();

    long getMax();

    int getRes();

    int getChannel();

    int getNoteNo(long var1);

    int getDynamic(long var1);
}
