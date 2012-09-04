/*
 * Copyright 2011 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tdwg.dwca.wikipedia.taxonbox;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base taxon info with all of the properties of interest.
 * The setters are not aligned to specifc tempalte property names in wikipedia,
 * that is done with the language specific sublcasses.
 */
abstract class TaxonInfoBase {
  protected static final Logger log = LoggerFactory.getLogger(TaxonInfo.class);

  // full params for debugging
  private Map<String,String> rawParams;
  //name
  private String scientificName;
  private String scientificNameAuthorship;
  private Rank rank;
  private String rankVerbatim;
  private String taxStatus;
  // holding temp names to collect all info before we can identify the lowest name & classification
  private List<Name> names = Lists.newArrayList();
  // classification
  private String kingdom;
  private String phylum;
  private String clazz;
  private String order;
  private String family;
  private String genus;
  private String subgenus;
  // synonyms
  private List<String> synonyms = Lists.newArrayList();
  private String synonymsRef;
  // lang => vernacular name
  private Set<String> vernacularNamesInDefaultLang = Sets.newHashSet();
  private Map<String, String> vernacularNames = Maps.newHashMap();
  // images
  private List<Image> images = Lists.newArrayList();
  // range maps
  private List<Image> rangeMaps = Lists.newArrayList();
  // conservation status
  private String status; // iucn status
  private String extinct; // year
  private String statusSystem;
  private String statusRef;
  private String fossilRange;
  private String fossilLocalities;
  private String fossilRangeFrom;
  private String fossilRangeTo;
  private Double fossilRangeFromMio;
  private Double fossilRangeToMio;
  private String trend;
  // types
  private String typeSpecies;
  private String typeSpeciesAuthority;
  private String typeGenus;
  private String typeGenusAuthority;
  // ?
  private String diversity; // c. 120species
  private String diversityLink;

  public void postprocess() {
    // set classification and scientific name from flexible rank names
    for (Name n : names) {
      if (n != null){
        setNameIfLowest(n);
        if (n.getRank()!=null && n.getScientific()!=null) {
          if (n.getRank()==Rank.Kingdom) {
            setKingdom(n.getScientific());
          } else if (n.getRank()==Rank.Phylum) {
            setPhylum(n.getScientific());
          } else if (n.getRank()==Rank.Class) {
            setClazz(n.getScientific());
          } else if (n.getRank()==Rank.Order) {
            setOrder(n.getScientific());
          } else if (n.getRank()==Rank.Family) {
            setFamily(n.getScientific());
          } else if (n.getRank()==Rank.Genus) {
            setGenus(n.getScientific());
          } else if (n.getRank()==Rank.Subgenus) {
            setSubgenus(n.getScientific());
          }
        }
      }
    }

    // replace genus abbreviation in name?
    if (!Strings.isNullOrEmpty(getGenus()) && Strings.nullToEmpty(getScientificName()).startsWith(getGenus().charAt(0)+".")) {
      String expandedName = getGenus() + " " + getScientificName().substring(2);
      log.debug("Expanding abbreviated name {} with {}", getScientificName(), expandedName);
      setScientificName(expandedName);
    }
  }

  protected Name name(int idx) {
    try {
      if (names.get(idx) == null) {
        names.set(idx, new Name());
      }
    } catch (IndexOutOfBoundsException e) {
      while(names.size() <= idx){
        names.add(null);
      }
      names.set(idx, new Name());
    }
    return names.get(idx);
  }

  public boolean hasScientificName() {
    return !Strings.isNullOrEmpty(scientificName);
  }

  public String getScientificName() {
    return scientificName;
  }

  public void setScientificName(String scientificName) {
    this.scientificName = scientificName;
  }

  public String getScientificNameAuthorship() {
    return scientificNameAuthorship;
  }

  public void setScientificNameAuthorship(String scientificNameAuthorship) {
    this.scientificNameAuthorship = scientificNameAuthorship;
  }

  protected void setScientificNameAndRankIfLowest(Rank rank, String name){
    if (getRank() == null || getRank().isHigherThan(rank)) {
      scientificName = name;
      this.rank = rank;
    }
  }

  protected void setNameIfLowest(Name name){
    if (!Strings.isNullOrEmpty(name.getScientific())) {
      if (getRank() == null || getRank().isHigherThan(name.getRank())) {
        rank = name.getRank();
        scientificName = name.getScientific();
        scientificNameAuthorship = name.getAuthor();
        //TODO: is this correct?
        addVernacularNameInDefaultLang(name.getVernacular());
      }
    }
  }

  /**
   * Only set authorship if current rank matches
   * @param rank
   * @param authorship
   */
  protected void setScientificNameAuthorship(Rank rank, String authorship){
    if (rank == getRank()) {
      scientificNameAuthorship = authorship;
    }
  }

  public Rank getRank() {
    return rank;
  }

  public void setRank(Rank rank) {
    this.rank = rank;
  }

  public String getRankVerbatim() {
    return rankVerbatim;
  }

  public void setRankVerbatim(String rankVerbatim) {
    this.rankVerbatim = rankVerbatim;
  }

  public String getTaxStatus() {
    return taxStatus;
  }

  public void setTaxStatus(String taxStatus) {
    this.taxStatus = taxStatus;
  }

  public String getKingdom() {
    return kingdom;
  }

  public void setKingdom(String kingdom) {
    this.kingdom = kingdom;
    setScientificNameAndRankIfLowest(Rank.Kingdom, kingdom);
  }

  public String getPhylum() {
    return phylum;
  }

  public void setPhylum(String phylum) {
    this.phylum = phylum;
    setScientificNameAndRankIfLowest(Rank.Phylum, phylum);
  }

  public String getClazz() {
    return clazz;
  }

  public void setClazz(String clazz) {
    this.clazz = clazz;
    setScientificNameAndRankIfLowest(Rank.Class, clazz);
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
    setScientificNameAndRankIfLowest(Rank.Order, order);
  }

  public String getFamily() {
    return family;
  }

  public void setFamily(String family) {
    this.family = family;
    setScientificNameAndRankIfLowest(Rank.Family, family);
  }

  public String getGenus() {
    return genus;
  }

  public void setGenus(String genus) {
    this.genus = genus;
    setScientificNameAndRankIfLowest(Rank.Genus, genus);
  }

  public String getSubgenus() {
    return subgenus;
  }

  public void setSubgenus(String subgenus) {
    this.subgenus = subgenus;
    setScientificNameAndRankIfLowest(Rank.Subgenus, subgenus);
  }

  public String getSynonymsRef() {
    return synonymsRef;
  }

  public void setSynonymsRef(String synonymsRef) {
    this.synonymsRef = synonymsRef;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExtinct() {
    return extinct;
  }

  public void setExtinct(String extinct) {
    this.extinct = extinct;
  }

  public String getStatusSystem() {
    return statusSystem;
  }

  public void setStatusSystem(String statusSystem) {
    this.statusSystem = statusSystem;
  }

  public String getStatusRef() {
    return statusRef;
  }

  public void setStatusRef(String statusRef) {
    this.statusRef = statusRef;
  }

  public void setFossilRange(String fossilRange) {
    this.fossilRange = fossilRange;
  }

  public String getFossilLocalities() {
    return fossilLocalities;
  }

  public void setFossilLocalities(String fossilLocalities) {
    this.fossilLocalities = fossilLocalities;
  }

  public String getFossilRangeFrom() {
    return fossilRangeFrom;
  }

  public void setFossilRangeFrom(String fossilRangeFrom) {
    this.fossilRangeFrom = fossilRangeFrom;
  }

  public String getFossilRangeTo() {
    return fossilRangeTo;
  }

  public void setFossilRangeTo(String fossilRangeTo) {
    this.fossilRangeTo = fossilRangeTo;
  }

  public Double getFossilRangeFromMio() {
    return fossilRangeFromMio;
  }

  public void setFossilRangeFromMio(Double fossilRangeFromMio) {
    this.fossilRangeFromMio = fossilRangeFromMio;
  }

  public Double getFossilRangeToMio() {
    return fossilRangeToMio;
  }

  public void setFossilRangeToMio(Double fossilRangeToMio) {
    this.fossilRangeToMio = fossilRangeToMio;
  }

  public String getTrend() {
    return trend;
  }

  public void setTrend(String trend) {
    this.trend = trend;
  }

  public String getTypeSpecies() {
    return typeSpecies;
  }

  public void setTypeSpecies(String typeSpecies) {
    this.typeSpecies = typeSpecies;
  }

  public String getTypeSpeciesAuthority() {
    return typeSpeciesAuthority;
  }

  public void setTypeSpeciesAuthority(String typeSpeciesAuthority) {
    this.typeSpeciesAuthority = typeSpeciesAuthority;
  }

  public String getTypeGenus() {
    return typeGenus;
  }

  public void setTypeGenus(String typeGenus) {
    this.typeGenus = typeGenus;
  }

  public String getTypeGenusAuthority() {
    return typeGenusAuthority;
  }

  public void setTypeGenusAuthority(String typeGenusAuthority) {
    this.typeGenusAuthority = typeGenusAuthority;
  }

  public String getDiversity() {
    return diversity;
  }

  public void setDiversity(String diversity) {
    this.diversity = diversity;
  }

  public String getDiversityLink() {
    return diversityLink;
  }

  public void setDiversityLink(String diversityLink) {
    this.diversityLink = diversityLink;
  }

  public List<Image> getRangeMaps() {
    return rangeMaps;
  }

  public List<String> getSynonyms() {
    return synonyms;
  }

  public Map<String, String> getVernacularNames() {
    return vernacularNames;
  }

  public List<Image> getImages() {
    return images;
  }

  protected Image image(int idx) {
    try {
      if (images.get(idx) == null) {
        images.set(idx, new Image());
      }
    } catch (IndexOutOfBoundsException e) {
      while(images.size() <= idx){
        images.add(null);
      }
      images.set(idx, new Image());
    }
    return images.get(idx);
  }

  protected Image rangeMap(int idx) {
    try {
      if (rangeMaps.get(idx) == null) {
        rangeMaps.set(idx, new Image());
      }
    } catch (IndexOutOfBoundsException e) {
      while (rangeMaps.size() <= idx) {
        rangeMaps.add(null);
      }
      rangeMaps.set(idx, new Image());
    }
    return rangeMaps.get(idx);
  }

  public Set<String> getVernacularNamesInDefaultLang() {
    return vernacularNamesInDefaultLang;
  }

  public void addVernacularNameInDefaultLang(String name) {
    vernacularNamesInDefaultLang.add(name);
  }

  public void addSynonym(String syn) {
    if (syn!=null){
      this.synonyms.add(syn);
    }
  }

  public Map<String, String> getRawParams() {
    return rawParams;
  }

  public void setRawParams(Map<String, String> rawParams) {
    this.rawParams = rawParams;
  }

  public String getFossilRange() {
    StringBuilder sb = new StringBuilder();
    if (!StringUtils.isBlank(fossilRange)){
      sb.append(fossilRange);
    }
    if (!StringUtils.isBlank(fossilRangeFrom) || !StringUtils.isBlank(fossilRangeTo)) {
      sb.append(" ");
      sb.append(StringUtils.trimToEmpty(fossilRangeFrom));

      sb.append(" - ");
      sb.append(StringUtils.trimToEmpty(fossilRangeTo));
    }
    if (fossilRangeFromMio!=null || fossilRangeToMio!=null) {
      sb.append(" ");
      sb.append(mioToString(fossilRangeFromMio));
      sb.append(" - ");
      sb.append(mioToString(fossilRangeToMio));
    }
    return StringUtils.trimToNull(sb.toString());
  }

  private String mioToString(Double mio){
    if (mio==null){
      return "";
    }
    return mio.toString();
  }
}
