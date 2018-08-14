package com.github.newk5.vcmp.javascript.resources;

import print.color.Ansi;
import print.color.ColoredPrinter;
import print.color.ColoredPrinterWIN;

public class ConsolePrinter {

    private ColoredPrinterWIN cpWin = null;
    private ColoredPrinter cpNix = null;

    public ConsolePrinter() {

        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            cpWin = new ColoredPrinterWIN.Builder(1, false).build();
        } else {
            cpNix = new ColoredPrinter.Builder(1, false).build();
        }
    }

    public void green(String msg) {
        if (cpWin != null) {
            cpWin.setForegroundColor(Ansi.FColor.GREEN);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setForegroundColor(Ansi.FColor.GREEN);
            cpNix.print(msg + "\r\n");
            cpNix.clear();

        }
    }

    public void message(Ansi.FColor frontColor, Ansi.BColor backColor, String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(backColor);
            cpWin.setForegroundColor(frontColor);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(backColor);
            cpNix.setForegroundColor(frontColor);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void print(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.BLACK);
            cpWin.setForegroundColor(Ansi.FColor.WHITE);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.BLACK);
            cpNix.setForegroundColor(Ansi.FColor.WHITE);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void error(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.BLACK);
            cpWin.setForegroundColor(Ansi.FColor.RED);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.BLACK);
            cpNix.setForegroundColor(Ansi.FColor.RED);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void warn(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.BLACK);
            cpWin.setForegroundColor(Ansi.FColor.YELLOW);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.BLACK);
            cpNix.setForegroundColor(Ansi.FColor.YELLOW);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void successBold(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.GREEN);
            cpWin.setForegroundColor(Ansi.FColor.BLACK);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.GREEN);
            cpNix.setForegroundColor(Ansi.FColor.BLACK);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void success(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.BLACK);
            cpWin.setForegroundColor(Ansi.FColor.GREEN);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.BLACK);
            cpNix.setForegroundColor(Ansi.FColor.GREEN);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

    public void errorBold(String msg) {

        if (cpWin != null) {

            cpWin.setBackgroundColor(Ansi.BColor.RED);
            cpWin.setForegroundColor(Ansi.FColor.BLACK);
            cpWin.print(cpWin.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpWin.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpWin.print(msg + "\r\n");
            cpWin.clear();
        } else {
            cpNix.setBackgroundColor(Ansi.BColor.RED);
            cpNix.setForegroundColor(Ansi.FColor.BLACK);
            cpNix.print(cpNix.getDateTime(), Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.WHITE);
            cpNix.print(" ", Ansi.Attribute.NONE, Ansi.FColor.BLACK, Ansi.BColor.BLACK);
            cpNix.print(msg + "\n");
            cpNix.clear();
        }

    }

}
