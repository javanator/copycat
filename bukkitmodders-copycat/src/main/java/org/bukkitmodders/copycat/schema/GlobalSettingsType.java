//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.07.06 at 08:25:44 PM CDT 
//


package org.bukkitmodders.copycat.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for globalSettingsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="globalSettingsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="prohibitedWorlds">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="world" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="maxImageWidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maxImageHeight" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="blockProfiles">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="blockProfile" type="{http://www.example.org/pluginSettings}blockProfileType" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "globalSettingsType", propOrder = {
    "prohibitedWorlds",
    "maxImageWidth",
    "maxImageHeight",
    "blockProfiles"
})
public class GlobalSettingsType {

    @XmlElement(required = true)
    protected GlobalSettingsType.ProhibitedWorlds prohibitedWorlds;
    protected int maxImageWidth;
    protected int maxImageHeight;
    @XmlElement(required = true)
    protected GlobalSettingsType.BlockProfiles blockProfiles;

    /**
     * Gets the value of the prohibitedWorlds property.
     * 
     * @return
     *     possible object is
     *     {@link GlobalSettingsType.ProhibitedWorlds }
     *     
     */
    public GlobalSettingsType.ProhibitedWorlds getProhibitedWorlds() {
        return prohibitedWorlds;
    }

    /**
     * Sets the value of the prohibitedWorlds property.
     * 
     * @param value
     *     allowed object is
     *     {@link GlobalSettingsType.ProhibitedWorlds }
     *     
     */
    public void setProhibitedWorlds(GlobalSettingsType.ProhibitedWorlds value) {
        this.prohibitedWorlds = value;
    }

    /**
     * Gets the value of the maxImageWidth property.
     * 
     */
    public int getMaxImageWidth() {
        return maxImageWidth;
    }

    /**
     * Sets the value of the maxImageWidth property.
     * 
     */
    public void setMaxImageWidth(int value) {
        this.maxImageWidth = value;
    }

    /**
     * Gets the value of the maxImageHeight property.
     * 
     */
    public int getMaxImageHeight() {
        return maxImageHeight;
    }

    /**
     * Sets the value of the maxImageHeight property.
     * 
     */
    public void setMaxImageHeight(int value) {
        this.maxImageHeight = value;
    }

    /**
     * Gets the value of the blockProfiles property.
     * 
     * @return
     *     possible object is
     *     {@link GlobalSettingsType.BlockProfiles }
     *     
     */
    public GlobalSettingsType.BlockProfiles getBlockProfiles() {
        return blockProfiles;
    }

    /**
     * Sets the value of the blockProfiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link GlobalSettingsType.BlockProfiles }
     *     
     */
    public void setBlockProfiles(GlobalSettingsType.BlockProfiles value) {
        this.blockProfiles = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="blockProfile" type="{http://www.example.org/pluginSettings}blockProfileType" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "blockProfile"
    })
    public static class BlockProfiles {

        @XmlElement(required = true)
        protected List<BlockProfileType> blockProfile;

        /**
         * Gets the value of the blockProfile property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the blockProfile property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBlockProfile().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link BlockProfileType }
         * 
         * 
         */
        public List<BlockProfileType> getBlockProfile() {
            if (blockProfile == null) {
                blockProfile = new ArrayList<BlockProfileType>();
            }
            return this.blockProfile;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="world" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "world"
    })
    public static class ProhibitedWorlds {

        protected List<String> world;

        /**
         * Gets the value of the world property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the world property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getWorld().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getWorld() {
            if (world == null) {
                world = new ArrayList<String>();
            }
            return this.world;
        }

    }

}