package com.liubs.jareditor.firstguide;

import com.intellij.openapi.application.ApplicationManager;
import com.liubs.jareditor.persistent.GuideStorage;
import com.liubs.jareditor.util.ClientVersions;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class FirstGuideStartup {
    private static volatile boolean alreadyCreated = false;

    public static void run() {

        //只创建一次，每个窗口都共用一个
        if(alreadyCreated){
            return;
        }
        synchronized (FirstGuideStartup.class) {
            if(alreadyCreated) {
                return;
            }
            alreadyCreated = true;
        }

        GuideStorage.getInstance().setVersion(ClientVersions.getCurrentPluginVersion());

        if(GuideStorage.getInstance().isShowed()){
            return;
        }
        GuideStorage.getInstance().setShowed(true);

        ApplicationManager.getApplication().invokeLater(() -> {
            try{
                FirstGuideDialog dialog = new FirstGuideDialog();
                dialog.show();
            }catch (Throwable e) {
                e.printStackTrace();
            }
        });

    }
}
