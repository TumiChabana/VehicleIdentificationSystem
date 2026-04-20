package com.vis.controller;

public class CustomerController extends BaseModuleController {
    @Override
    protected void onUserLoaded() {
        System.out.println("Customer loaded for: "
                + currentUser.getUsername());
    }
}