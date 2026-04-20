package com.vis.controller;

public class PoliceController extends BaseModuleController {
    @Override
    protected void onUserLoaded() {
        System.out.println("Police loaded for: "
                + currentUser.getUsername());
    }
}