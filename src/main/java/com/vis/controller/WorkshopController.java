package com.vis.controller;

public class WorkshopController extends BaseModuleController {
    @Override
    protected void onUserLoaded() {
        System.out.println("Workshop loaded for: "
                + currentUser.getUsername());
    }
}