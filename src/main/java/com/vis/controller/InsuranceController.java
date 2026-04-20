package com.vis.controller;

public class InsuranceController extends BaseModuleController {
    @Override
    protected void onUserLoaded() {
        System.out.println("Insurance loaded for: "
                + currentUser.getUsername());
    }
}