package com.silverpeas.kmelia.updatechainhelpers;

import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class DefineUnpromptedCandidacy extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) {
    // r�cup�ration des donn�es
    PublicationDetail pubDetail = uchc.getPubDetail();

    // concat�nation de "description" et "mot cl�"
    String newDescription = pubDetail.getDescription() + " \n"
        + pubDetail.getKeywords();
    pubDetail.setDescription(newDescription);
    pubDetail.setKeywords("");
    uchc.setPubDetail(pubDetail);
  }

}
