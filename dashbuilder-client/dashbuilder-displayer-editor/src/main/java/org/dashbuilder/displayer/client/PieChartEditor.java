/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.displayer.client;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.displayer.DataDisplayerColumn;
import org.dashbuilder.displayer.PieChartDisplayer;
import org.dashbuilder.displayer.client.widgets.CommonAttributesEditor;
import org.dashbuilder.displayer.impl.DataDisplayerColumnImpl;

/**
 * Pie chart editor.
 */
@ApplicationScoped
@Named("piechart_editor")
public class PieChartEditor extends AbstractDisplayerEditor<PieChartDisplayer> {

    interface EditorBinder extends UiBinder<Widget, PieChartEditor>{}
    private static final EditorBinder uiBinder = GWT.create(EditorBinder.class);

    @UiField
    HorizontalPanel commonAttributesPanel;

    private CommonAttributesEditor commonAttributesEditor;

    public PieChartEditor() {

        // Init the editor from the UI Binder template
        initWidget(uiBinder.createAndBindUi(this));

        // TODO This nested widget setup really requires an MVP oriented approach
        commonAttributesEditor = new CommonAttributesEditor();

        commonAttributesEditor.addShowTitleChangeHandler( new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange( ValueChangeEvent<Boolean> event ) {
                dataDisplayer.setTitleVisible( event.getValue() );
                notifyChanges();
            }
        } );

        commonAttributesEditor.addTitleChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                String title = event.getValue();
                if ( title != null ) {
                    dataDisplayer.setTitle( title );
                    notifyChanges();
                }
            }
        } );

        commonAttributesEditor.addColumnsChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange( ValueChangeEvent<String> event ) {
                dataDisplayer.getColumnList().clear();
                dataDisplayer.getColumnList().addAll( parseColumns( event.getValue() ) );
                notifyChanges();
            }
        } );

        commonAttributesPanel.add( commonAttributesEditor );
    }

    @Override
    public void setDataDisplayer( PieChartDisplayer dataDisplayer ) {
        super.setDataDisplayer( dataDisplayer );
        commonAttributesEditor.setIsTitleVisible( dataDisplayer.isTitleVisible() );
        commonAttributesEditor.setTitle( dataDisplayer.getTitle() );
        commonAttributesEditor.setColumns( formatColumns( dataDisplayer.getColumnList() ) );
    }

    private List<DataDisplayerColumn> parseColumns( String columns ) {
        if ( columns.length() > 0) {
            String[] sa = columns.split( "," );
            List<DataDisplayerColumn> l = new ArrayList<DataDisplayerColumn>( sa.length );
            for ( int i = 0; i < sa.length; i++ ) {
                DataDisplayerColumnImpl ddci = new DataDisplayerColumnImpl();
                String[] idAlias = sa[i].trim().split( ":" );
                if ( idAlias.length == 2 ) {
                    if ( StringUtils.isBlank( idAlias[ 0 ] ) && StringUtils.isBlank( idAlias[1] ) )
                        throw new IllegalArgumentException( "You must specify at least a column alias." );

                    if ( !StringUtils.isBlank( idAlias[1] ) ) {
                        ddci.setDisplayName( idAlias[ 1 ].trim() );
                    } else ddci.setDisplayName( idAlias[0].trim() );

                    if ( !StringUtils.isBlank( idAlias[0] ) ) ddci.setColumnId( idAlias[0].trim() );

                } else {
                    if ( !StringUtils.isBlank( idAlias[0] ) ) ddci.setDisplayName( idAlias[0].trim() );
                    else throw new IllegalArgumentException( "You must specify at least a column alias." );
                }
                l.add( ddci );
            }
            return l;
        }
        return new ArrayList<DataDisplayerColumn>();
    }

    private String formatColumns( List<DataDisplayerColumn> columns ) {
        StringBuilder sb = new StringBuilder( "" );
        if ( columns != null ) {
            for ( int i = 0; i < columns.size(); i++ ) {
                String columnId = columns.get( i ).getColumnId();
                if ( !StringUtils.isBlank( columnId ) ) {
                    sb.append( columnId ).append( ":" );
                }
                sb.append( columns.get( i ).getDisplayName() );
                if ( i != columns.size() -1 ) sb.append( "," );
            }
        }
        return sb.toString();
    }
}