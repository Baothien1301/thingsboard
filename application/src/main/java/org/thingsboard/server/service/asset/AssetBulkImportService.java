/**
 * Copyright © 2016-2021 The Thingsboard Authors
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
package org.thingsboard.server.service.asset;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.asset.AssetService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.importing.AbstractBulkImportService;
import org.thingsboard.server.service.importing.BulkImportColumnType;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Map;
import java.util.Optional;

@Service
@TbCoreComponent
@RequiredArgsConstructor
public class AssetBulkImportService extends AbstractBulkImportService<Asset> {
    private final AssetService assetService;

    @Override
    protected void setEntityFields(Asset entity, Map<BulkImportColumnType, String> fields) {
        ObjectNode additionalInfo = (ObjectNode) Optional.ofNullable(entity.getAdditionalInfo()).orElseGet(JacksonUtil::newObjectNode);
        fields.forEach((columnType, value) -> {
            switch (columnType) {
                case NAME:
                    entity.setName(value);
                    break;
                case TYPE:
                    entity.setType(value);
                    break;
                case LABEL:
                    entity.setLabel(value);
                    break;
                case DESCRIPTION:
                    additionalInfo.set("description", new TextNode(value));
                    break;
            }
        });
        entity.setAdditionalInfo(additionalInfo);
    }

    @Override
    protected Asset saveEntity(Asset entity, Map<BulkImportColumnType, String> fields) {
        return assetService.saveAsset(entity);
    }

    @Override
    protected Asset findOrCreateEntity(TenantId tenantId, String name) {
        return Optional.ofNullable(assetService.findAssetByTenantIdAndName(tenantId, name))
                .orElseGet(Asset::new);
    }

    @Override
    protected void setOwners(Asset entity, SecurityUser user) {
        entity.setTenantId(user.getTenantId());
        entity.setCustomerId(user.getCustomerId());
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.ASSET;
    }

}
