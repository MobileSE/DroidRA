package lu.uni.snt.droidra.serviceimpl;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.GlobalRef;
import lu.uni.snt.droidra.model.DroidRAConstant;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtValue;
import lu.uni.snt.droidra.service.CoalResultAccuracyVerifyService;
import lu.uni.snt.droidra.typeref.soot.methodrelated.ClassMethodParamTypesKey;
import lu.uni.snt.droidra.util.ApplicationClassFilter;
import lu.uni.snt.droidra.util.TypeConversionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoalResultAccuracyVerifyServiceImpl implements CoalResultAccuracyVerifyService {

    @Override
    public Map<StmtKey, StmtValue> verifyCoalResult(Map<StmtKey, StmtValue> stmtKeyValues) {
        Map<StmtKey, StmtValue> newStmtKeyValues = new HashMap<StmtKey, StmtValue>();

        Map<ClassMethodParamTypesKey, String> classMethodParamTypesKeyStringMap = GlobalRef.classMethodParamTypesKeyStringMap;

        stmtKeyValues.entrySet().stream().forEach(entry -> {
            StmtKey key = entry.getKey();
            StmtValue value = entry.getValue();

            Set<ClassDescription> oldSet = new HashSet<ClassDescription>();
            Set<ClassDescription> newSet = new HashSet<ClassDescription>();

            value.getClsSet().stream().filter(clsDesc -> {
                return null != clsDesc.cls && !clsDesc.cls.contains(DroidRAConstant.OPTIMIZED);
            }).forEach(clsDesc -> {
                String clsName = clsDesc.cls;
                String name = clsDesc.name;
                if (StringUtils.isNotBlank(clsName) && !StringUtils.equals(clsName, DroidRAConstant.STAR_SYMBOL)
                        && StringUtils.isNotBlank(name) && !StringUtils.equals(name, DroidRAConstant.STAR_SYMBOL)) {

                    switch (value.getType()) {
                        case FIELD_CALL:
                            //If you only know the field name and type, you can't find the class it belongs to.
                            break;
                        case METHOD_CALL:
//                            if(!ApplicationClassFilter.isApplicationClass(clsName)){
//                                break;
//                            }
                            ClassMethodParamTypesKey classMethodParamTypesKey = new ClassMethodParamTypesKey();
                            classMethodParamTypesKey.cls = clsName;
                            classMethodParamTypesKey.method = name;
                            classMethodParamTypesKey.paramTypes = TypeConversionUtil.convertSootParamtypes2String(key.getStmt().getInvokeExpr().getMethod().getParameterTypes());

                            String classMethodParamTypesKeyString = classMethodParamTypesKeyStringMap.get(classMethodParamTypesKey);

                            if (StringUtils.isBlank(classMethodParamTypesKeyString)) {
                                oldSet.add(clsDesc);

                                ClassDescription cd = new ClassDescription();
                                cd.cls = DroidRAConstant.OPTIMIZED + clsName;
                                cd.name = DroidRAConstant.OPTIMIZED + name;
                                newSet.add(cd);
                            }
                            break;
                        default:    //SIMPLE_STRING
                            break;
                    }
                }
            });

            value.getClsSet().removeAll(oldSet);
            value.getClsSet().addAll(newSet);

            newStmtKeyValues.put(key, value);
        });

        return newStmtKeyValues;
    }

}