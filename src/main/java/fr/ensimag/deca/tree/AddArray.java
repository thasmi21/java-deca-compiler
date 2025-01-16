package fr.ensimag.deca.tree;

import fr.ensimag.deca.syntax.DecaParser;

import java.util.List;

public class AddArray extends CallMethodArray{
    public AddArray(List<DecaParser.ExprContext> list_tab) {
        super(list_tab, "add");
    }
}
