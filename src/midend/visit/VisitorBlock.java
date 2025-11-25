package midend.visit;

import frontend.parser.ast.stmt.Block;
import frontend.parser.ast.stmt.BlockItem;

import java.util.ArrayList;

public class VisitorBlock {
    public static void visitBlock(Block block) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            visitBlockItem(blockItem);
        }
    }

    private static void visitBlockItem(BlockItem blockItem) {
        if (blockItem.isDecl()) {
            VisitorDecl.visitDecl(blockItem.getDecl());
        } else {
            VisitorStmt.visitStmt(blockItem.getStmt());
        }
    }
}
