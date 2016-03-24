package org.silverpeas.components.gallery;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.components.gallery.service.MediaServiceProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.FileUtil;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.File;

/**
 * @author Yohann Chastagnier
 */
@Named
public class GalleryInstancePreDestruction implements ComponentInstancePreDestruction {

  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    MediaServiceProvider.getMediaService()
        .deleteAlbum(UserDetail.getCurrentRequester(), componentInstanceId,
            new NodePK(NodePK.ROOT_NODE_ID, componentInstanceId));
    FileUtil.deleteEmptyDir(new File(FileRepositoryManager.getAbsolutePath(componentInstanceId)));
  }
}
