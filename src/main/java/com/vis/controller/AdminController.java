package com.vis.controller;

public class AdminController extends BaseModuleController {
    @Override
    protected void onUserLoaded() {
        System.out.println("Admin loaded for: "
                + currentUser.getUsername());
    }
}