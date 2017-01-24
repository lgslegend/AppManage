/**
 * @author Mohammad Saiful Alam
 * FIXME
 */
package com.codeartist.applocker.listener;

public interface NotifyRemovalObserver {
    void completeRemoveFiles();

    void confirmationBtnClick(boolean isUninstall);
}