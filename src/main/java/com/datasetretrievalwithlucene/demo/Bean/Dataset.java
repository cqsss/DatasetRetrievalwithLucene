package com.datasetretrievalwithlucene.demo.Bean;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Dataset {

  public Dataset() {
  }

  private String licenseTitle;
  private String maintainer;
  private String maintainerEmail;
  private String id;
  private String metadataCreated;
  private String metadataModified;
  private String author;
  private String authorEmail;
  private String state;
  private String version;
  private String creatorUserId;
  private String type;
  private String licenseId;
  private String name;
  private String url;
  private String notes;
  private String ownerOrg;
  private String title;
  private String revisionId;
  private String orgDescription;
  private String orgCreated;
  private String orgTitle;
  private String orgName;
  private long orgIsOrganization;
  private String orgState;
  private String orgImageUrl;
  private String orgRevisionId;
  private String orgType;
  private String orgId;
  private String orgApprovalStatus;
  private long isprivate;
  private long numTags;
  private long numResources;
  private long isopen;
  private String dataSource;
  private long localId;


  public String getLicenseTitle() {
    return licenseTitle;
  }

  public void setLicenseTitle(String licenseTitle) {
    this.licenseTitle = licenseTitle;
  }


  public String getMaintainer() {
    return maintainer;
  }

  public void setMaintainer(String maintainer) {
    this.maintainer = maintainer;
  }


  public String getMaintainerEmail() {
    return maintainerEmail;
  }

  public void setMaintainerEmail(String maintainerEmail) {
    this.maintainerEmail = maintainerEmail;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  public String getMetadataCreated() {
    return metadataCreated;
  }

  public void setMetadataCreated(String metadataCreated) {
    this.metadataCreated = metadataCreated;
  }


  public String getMetadataModified() {
    return metadataModified;
  }

  public void setMetadataModified(String metadataModified) {
    this.metadataModified = metadataModified;
  }


  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }


  public String getAuthorEmail() {
    return authorEmail;
  }

  public void setAuthorEmail(String authorEmail) {
    this.authorEmail = authorEmail;
  }


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  public String getCreatorUserId() {
    return creatorUserId;
  }

  public void setCreatorUserId(String creatorUserId) {
    this.creatorUserId = creatorUserId;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getLicenseId() {
    return licenseId;
  }

  public void setLicenseId(String licenseId) {
    this.licenseId = licenseId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }


  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }


  public String getOwnerOrg() {
    return ownerOrg;
  }

  public void setOwnerOrg(String ownerOrg) {
    this.ownerOrg = ownerOrg;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }


  public String getRevisionId() {
    return revisionId;
  }

  public void setRevisionId(String revisionId) {
    this.revisionId = revisionId;
  }


  public String getOrgDescription() {
    return orgDescription;
  }

  public void setOrgDescription(String orgDescription) {
    this.orgDescription = orgDescription;
  }


  public String getOrgCreated() {
    return orgCreated;
  }

  public void setOrgCreated(String orgCreated) {
    this.orgCreated = orgCreated;
  }


  public String getOrgTitle() {
    return orgTitle;
  }

  public void setOrgTitle(String orgTitle) {
    this.orgTitle = orgTitle;
  }


  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }


  public long getOrgIsOrganization() {
    return orgIsOrganization;
  }

  public void setOrgIsOrganization(long orgIsOrganization) {
    this.orgIsOrganization = orgIsOrganization;
  }


  public String getOrgState() {
    return orgState;
  }

  public void setOrgState(String orgState) {
    this.orgState = orgState;
  }


  public String getOrgImageUrl() {
    return orgImageUrl;
  }

  public void setOrgImageUrl(String orgImageUrl) {
    this.orgImageUrl = orgImageUrl;
  }


  public String getOrgRevisionId() {
    return orgRevisionId;
  }

  public void setOrgRevisionId(String orgRevisionId) {
    this.orgRevisionId = orgRevisionId;
  }


  public String getOrgType() {
    return orgType;
  }

  public void setOrgType(String orgType) {
    this.orgType = orgType;
  }


  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }


  public String getOrgApprovalStatus() {
    return orgApprovalStatus;
  }

  public void setOrgApprovalStatus(String orgApprovalStatus) {
    this.orgApprovalStatus = orgApprovalStatus;
  }


  public long getIsprivate() {
    return isprivate;
  }

  public void setIsprivate(long isprivate) {
    this.isprivate = isprivate;
  }


  public long getNumTags() {
    return numTags;
  }

  public void setNumTags(long numTags) {
    this.numTags = numTags;
  }


  public long getNumResources() {
    return numResources;
  }

  public void setNumResources(long numResources) {
    this.numResources = numResources;
  }


  public long getIsopen() {
    return isopen;
  }

  public void setIsopen(long isopen) {
    this.isopen = isopen;
  }


  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }


  public long getLocalId() {
    return localId;
  }

  public void setLocalId(long localId) {
    this.localId = localId;
  }

}
