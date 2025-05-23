package com.fourt.railskylines.decorator;

public abstract class PriceDecorator implements PriceCalculator {
    protected PriceCalculator calculator;

    public PriceDecorator(PriceCalculator calculator) {
        this.calculator = calculator;
    }
}