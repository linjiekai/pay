/*
 * 版权所有 (C) 2013 中国南方航空股份有限公司。
 * 本文件可能包含有南方航空公司的机密或专有信息。
 * 未经许可不得擅自公开、复制这些机密资料及其中任何部分，
 * 只可按照其使用许可协议，在南方航空公司内部使用。
 *
 * File Name: AbstractObject.java
 * Creation Date: 2013-3-29 下午2:23:12
 * Author: Kyon
 */
package com.mppay.core.config;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;


/**
 * Common part of domain object.<br>
 * Beauty style {@code toString()} embedded, automatically reflection
 * {@code equals(Object)}, distributed unique {@code hashCode()} and
 * {@code clone()} supported, {@code identityHashCode()} and
 * {@code toObjectId()} also offered.
 *
 * @author Kyon
 */
@SuppressWarnings("serial")
public abstract class AbstractObject implements Serializable, Cloneable {

    public AbstractObject() {
        super();
    }

    public AbstractObject(String xml) throws JAXBException {
        this.fromXml(xml);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Build a identity hash code.
     *
     * @return int hash code
     * @see System#identityHashCode(Object)
     */
    public int identityHashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Returns a string representation of the object. Apache commons-lang's
     * <code>ToStringBuilder.reflectionToString</code> is invoking to build the
     * full map style (<code>ToStringStyle.SHORT_PREFIX_STYLE</code>) string, In
     * other words, this method returns a string equal to the value of:
     * <blockquote>
     *
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * + '[' + fields in class + ']'
     * </pre>
     *
     * </blockquote>
     *
     * @return a string representation of the object.
     * @see ToStringBuilder#reflectionToString(Object, ToStringStyle)
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SIMPLE_STYLE);
    }

    /**
     * Returns a string representation of the object with XML style, this works
     * when and only when sub class is well-annotated by JAXB annotation.
     *
     * @param formatted indicates if output XML is a pretty formatted XML string.
     * @return a XML-formed string representation of the object.
     * @throws JAXBException when JAXB marshaller does not work
     */
    public String toXml(boolean formatted) throws JAXBException {
        StringWriter sw = new StringWriter();
        Marshaller marshall = JaxbContextHolder.getMarshaller(this.getClass());
        marshall.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted);
        marshall.marshal(this, sw);
        return sw.toString();
    }

    /**
     * Returns a object representation from the XML string, this works when and
     * only when sub class is well-annotated by JAXB annotation.
     *
     * @param xml   a XML string representation of the class.
     * @param clazz class to representation.
     * @return an object instance parsed from the string.
     * @throws JAXBException when JAXB unmarshaller does not work
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXml(String xml, Class<T> clazz)
            throws JAXBException {
        Unmarshaller unmarshaller = JaxbContextHolder.getUnmarshaller(clazz);
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }

    /**
     * Returns a object representation from the XML string, this works when and
     * only when sub class is well-annotated by JAXB annotation.
     *
     * @param xml a XML string representation of the class.
     * @return an object instance parsed from the string.
     * @throws JAXBException
     */
    @SuppressWarnings("unchecked")
    public <T> T fromXml(String xml)
            throws JAXBException {
        return (T) fromXml(xml, this.getClass());
    }

    /**
     * Returns a string representation of the object similar with
     * <code>Object.toString()</code> method. It equals to the value of:
     * <blockquote>
     *
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this))
     * </pre>
     *
     * </blockquote>
     *
     * @return a string representation of the object.
     */
    public String toObjectId() {
        return getClass().getName() + "@"
                + Integer.toHexString(identityHashCode());
    }

    /**
     * Creates and returns a copy of this object. The precise meaning of "copy"
     * may depend on the class of the object. The general intent is that, for
     * any object <tt>x</tt>, the expression: <blockquote>
     *
     * <pre>
     * x.clone() != x
     * </pre>
     *
     * </blockquote> will be true, and that the expression: <blockquote>
     *
     * <pre>
     * x.clone().getClass() == x.getClass()
     * </pre>
     *
     * </blockquote> will be <tt>true</tt>, but these are not absolute
     * requirements. While it is typically the case that: <blockquote>
     *
     * <pre>
     * x.clone().equals(x)
     * </pre>
     *
     * </blockquote> will be <tt>true</tt>, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling
     * <tt>super.clone</tt>. If a class and all of its superclasses (except
     * <tt>Object</tt>) obey this convention, it will be the case that
     * <tt>x.clone().getClass() == x.getClass()</tt>.
     * <p>
     * By convention, the object returned by this method should be independent
     * of this object (which is being cloned). To achieve this independence, it
     * may be necessary to modify one or more fields of the object returned by
     * <tt>super.clone</tt> before returning it. Typically, this means copying
     * any mutable objects that comprise the internal "deep structure" of the
     * object being cloned and replacing the references to these objects with
     * references to the copies. If a class contains only primitive fields or
     * references to immutable objects, then it is usually the case that no
     * fields in the object returned by <tt>super.clone</tt> need to be
     * modified.
     * <p>
     * The method <tt>clone</tt> for class <tt>Object</tt> performs a specific
     * cloning operation. First, if the class of this object does not implement
     * the interface <tt>Cloneable</tt>, then a
     * <tt>CloneNotSupportedException</tt> is thrown. Note that all arrays are
     * considered to implement the interface <tt>Cloneable</tt>. Otherwise, this
     * method creates a new instance of the class of this object and initializes
     * all its fields with exactly the contents of the corresponding fields of
     * this object, as if by assignment; the contents of the fields are not
     * themselves cloned. Thus, this method performs a "shallow copy" of this
     * object, not a "deep copy" operation.
     * <p>
     * The class <tt>Object</tt> does not itself implement the interface
     * <tt>Cloneable</tt>, so calling the <tt>clone</tt> method on an object
     * whose class is <tt>Object</tt> will result in returning null.
     *
     * @return a clone of this instance. null will be returned if class is not
     * cloneable.
     * @see Cloneable
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
