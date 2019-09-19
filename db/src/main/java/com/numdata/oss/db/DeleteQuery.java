/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numdata.oss.db;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Builder for DELETE queries.
 *
 * @param <T> Table type.
 *
 * @author Peter S. Heijnen
 */
public class DeleteQuery<T>
extends AbstractQuery<T>
{
	/**
	 * Create query builder.
	 */
	public DeleteQuery()
	{
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableName Table name.
	 */
	public DeleteQuery( @NotNull final String tableName )
	{
		setTableName( tableName );
	}

	/**
	 * Create query builder for the specified table.
	 *
	 * @param tableClass Table record class.
	 */
	public DeleteQuery( @NotNull final Class<T> tableClass )
	{
		setTableClass( tableClass );
	}

	@NotNull
	@Override
	public String getQueryString( @NotNull final String tableName )
	{
		final StringBuilder sb = new StringBuilder();

		final CharSequence join = getJoin();
		final boolean hasJoin = !TextTools.isEmpty( join );

		sb.append( "DELETE " );
		if ( hasJoin )
		{
			sb.append( tableName );
		}
		sb.append( getSeparator() );
		sb.append( "FROM " );
		sb.append( tableName );

		final String tableAlias = getTableAlias();
		if ( !TextTools.isEmpty( tableAlias ) )
		{
			sb.append( " AS " );
			sb.append( tableAlias );
		}

		if ( hasJoin )
		{
			sb.append( getSeparator() );
			sb.append( join );
		}

		final CharSequence where = getWhere();
		if ( !TextTools.isEmpty( where ) )
		{
			sb.append( getSeparator() );
			sb.append( "WHERE " );
			sb.append( where );
		}

		return sb.toString();
	}

	/**
	 * Get parameter values used in the query.
	 *
	 * @return Query parameters (may be empty).
	 */
	@Override
	@NotNull
	public Object[] getQueryParameters()
	{
		return toArray( getJoinParameters(), getWhereParameters() );
	}
}
