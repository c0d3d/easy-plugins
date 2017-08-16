package com.nlocketz.internal;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Map;

abstract class AbstractPluginFileGenerator implements PluginFileGenerator {
    protected Types types;
    protected Elements elements;
    protected RoundEnvironment roundEnv;
    protected Map<String, String> options;

    private Messager msgr;

    protected AbstractPluginFileGenerator(ProcessingEnvironment procEnv, RoundEnvironment roundEnv) {
        this.types = procEnv.getTypeUtils();
        this.elements = procEnv.getElementUtils();
        this.roundEnv = roundEnv;
        this.msgr = procEnv.getMessager();
        this.options = procEnv.getOptions();
    }

    private void sendMsg(Diagnostic.Kind kind, String msg) {
        msgr.printMessage(kind, msg);
    }

    protected void error(String message) {
        sendMsg(Diagnostic.Kind.ERROR, message);
    }

    protected void info(String message) {
        sendMsg(Diagnostic.Kind.NOTE, message);
    }

}
