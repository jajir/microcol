package org.akropia.gui.screen.editor;

import org.akropia.model.store.ModelPo;

import com.google.common.base.Preconditions;

class MouseOperationContext {

    private final ModelService modelService;

    MouseOperationContext(final ModelService modelService) {
        this.modelService = Preconditions.checkNotNull(modelService);
    }

    public ModelPo getModelPo() {
        return modelService.getModelPo();
    }

}
