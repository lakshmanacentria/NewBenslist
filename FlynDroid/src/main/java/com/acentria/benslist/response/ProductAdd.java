package com.acentria.benslist.response;

public class ProductAdd {
    private String prodcutname;
    private String prodcut_qauntity;

    public ProductAdd(String prodcutname, String prodcut_qauntity) {
        this.prodcutname = prodcutname;
        this.prodcut_qauntity = prodcut_qauntity;
    }

    public String getProdcutname() {
        return prodcutname;
    }

    public void setProdcutname(String prodcutname) {
        this.prodcutname = prodcutname;
    }

    public String getProdcut_qauntity() {
        return prodcut_qauntity;
    }

    public void setProdcut_qauntity(String prodcut_qauntity) {
        this.prodcut_qauntity = prodcut_qauntity;
    }
}
