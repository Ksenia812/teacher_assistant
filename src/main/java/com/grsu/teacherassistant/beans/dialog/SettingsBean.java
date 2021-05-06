package com.grsu.teacherassistant.beans.dialog;

import com.grsu.teacherassistant.utils.FacesUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import static com.grsu.teacherassistant.utils.FacesUtils.closeDialog;
import static com.grsu.teacherassistant.utils.FacesUtils.update;

@ManagedBean(name = "settingsBean")
@ViewScoped
@Data
public class SettingsBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsBean.class);
    private static final String SETTINGS_DIALOG_NAME = "settingsDialog";

    public void init() {
        FacesUtils.showDialog(SETTINGS_DIALOG_NAME);
    }

    public void exit() {
        closeDialog(SETTINGS_DIALOG_NAME);
    }

    public void save() {
        update("views");
        exit();
    }

}
