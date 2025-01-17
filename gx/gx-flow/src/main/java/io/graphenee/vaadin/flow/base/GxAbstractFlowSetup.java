package io.graphenee.vaadin.flow.base;

import java.util.List;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.router.RouterLayout;

import io.graphenee.core.model.GxAuthenticatedUser;
import io.graphenee.vaadin.flow.utils.DashboardUtils;

public abstract class GxAbstractFlowSetup {

    public String appTitle() {
        return "Flow Application";
    }

    public String appVersion() {
        return "1.0";
    }

    public String appTitleWithVersion() {
        return String.format("%1$s v%2$s", appTitle(), appVersion());
    }

    public String defaultRoute() {
        return "";
    }

    public GxAuthenticatedUser loggedInUser() {
        return DashboardUtils.getLoggedInUser();
    }

    public abstract Class<? extends RouterLayout> routerLayout();

    public abstract List<GxMenuItem> menuItems();

    public Image appLogo() {
        return null;
    }

}
