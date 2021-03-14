package com.grsu.teacherassistant.beans.dialog;

import com.grsu.teacherassistant.utils.ApplicationUtils;
import com.grsu.teacherassistant.utils.FacesUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import static com.grsu.teacherassistant.utils.FacesUtils.closeDialog;
import static com.grsu.teacherassistant.utils.FacesUtils.update;
import static com.grsu.teacherassistant.utils.PropertyUtils.AUTO_BACKUP_NEW_MODE_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.AUTO_BACKUP_PROPERTY_NAME;
import static com.grsu.teacherassistant.utils.PropertyUtils.setProperty;

@ManagedBean(name = "backupBean")
@ViewScoped
@Data
public class BackupBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupBean.class);
    private static final String BACKUP_DIALOG = "backupDialog";

    private Boolean autoBackup;
    private Boolean autoBackupOnNewMode;

    public void init() {
        autoBackup = ApplicationUtils.isAutoBackupEnabled();
        autoBackupOnNewMode = ApplicationUtils.isAutoBackupOnNewModeEnabled();

        FacesUtils.showDialog(BACKUP_DIALOG);
    }

    public void exit() {
        closeDialog(BACKUP_DIALOG);
    }

    public void save() {
        setProperty(AUTO_BACKUP_PROPERTY_NAME, autoBackup.toString());
        setProperty(AUTO_BACKUP_NEW_MODE_PROPERTY_NAME, autoBackupOnNewMode.toString());
        update("views");
        exit();
    }

}
