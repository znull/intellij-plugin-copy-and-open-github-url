// Copyright 2013 Square, Inc.
package com.squareup.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import java.awt.datatransfer.StringSelection;

public class CopyGithubUrlPath extends AnAction {
  private static final Logger LOG = Logger.getInstance("#" + CopyGithubUrlPath.class.getName());

  /**
   * MVP: copies current file name. then appropriate github prefix. If a line is selected, it
   * supports that in the URL, too.
   *
   * By design it does not support: branches, folders, or multiple files selected (it picks the
   * 1st).
   */
  public void actionPerformed(AnActionEvent event) {
    Project project = event.getData(PlatformDataKeys.PROJECT);
    final Editor editor = event.getData(PlatformDataKeys.EDITOR);
    final VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
    Integer line = (editor != null)
        ? editor.getSelectionModel().getSelectionStartPosition().getLine() + 1 : null;
    copyUrl(project, file, line);
    showStatusBubble(event, file);
  }

  private void copyUrl(Project project, VirtualFile file, Integer line) {
    String basePath = project.getBasePath();
    GithubRepo githubRepo = new GithubRepo(basePath);
    String relativeFilePath = file.getCanonicalPath().replaceFirst(basePath, "");
    String url = githubRepo.repoUrlFor(relativeFilePath, line);
    CopyPasteManager.getInstance().setContents(new StringSelection(url));
  }

  private void showStatusBubble(AnActionEvent event, VirtualFile file) {
    StatusBar statusBar = WindowManager.getInstance()
        .getStatusBar(DataKeys.PROJECT.getData(event.getDataContext()));

    JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(
            "<p>Github URL for '<tt>"
                + file.getPresentableName()
                + "</tt>' (on master branch) copied to your clipboard.</p>",
            MessageType.INFO, null)
        .setFadeoutTime(5500)
        .createBalloon()
        .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
  }
}