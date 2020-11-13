/*
 * (C) Copyright Numdata BV 2015-2015 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.lang.annotation.*;
import javax.ws.rs.*;

/**
 * Annotation to enable Cross-Origin Resource Sharing.
 *
 * @author Gerrit Meinders
 */
@Target( { ElementType.TYPE, ElementType.METHOD } )
@Retention( RetentionPolicy.RUNTIME )
@NameBinding
public @interface CrossOrigin
{
}
