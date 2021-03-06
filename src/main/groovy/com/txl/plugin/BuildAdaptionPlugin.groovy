package com.txl.plugin

import com.txl.plugin.task.ModuleDeleteAdaptionTask
import com.txl.plugin.xmlutils.StringUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.txl.plugin.task.ModuleAdaptionTask
import org.gradle.api.tasks.TaskProvider


/**
 * 不仅提供创建适配的能力，还需要有地方删除对应的目录
 * */
class BuildAdaptionPlugin implements Plugin<Project> {
    def BuildAdaptionPluginExtension appExtension
    void apply(Project project) {
        System.out.println("start adaption version 1.0.0")
        appExtension = project.extensions.create('adaptionAppExtension', BuildAdaptionPluginExtension,project)//全局扩展属性
        appExtension.resPath = "${File.separator}src${File.separator}main${File.separator}res${File.separator}"
        appExtension.defaultDesignWidth = 360f
        project.afterEvaluate {
            handleProject(project.rootProject)
        }
    }

    private void handleProject(Project project){
        def subProjects = project.subprojects
        for (item in subProjects){
            handleProject(item)
        }
        println("config project ${project.name}  has preBuild Task ${project.hasProperty("preBuild")}")
        def taskModuleAdaption = project.tasks.register("${project.name}BuildAdaption",ModuleAdaptionTask)
        if(project.hasProperty("preBuild")){//已经拥有该属性，说明project  Evaluate 阶段已经完成
            handleModuleCreateAdaptionTask(project, taskModuleAdaption)
        }else {
            project.afterEvaluate{
                if(project.hasProperty("preBuild")){//在Evaluate之后还没有改属性说明不是android项目，不需要建立依赖关系
                    handleModuleCreateAdaptionTask(project, taskModuleAdaption)
                }
            }
        }
        def taskDeleteModuleAdaption = project.tasks.register("${project.name}DeleteBuildAdaption",ModuleDeleteAdaptionTask)
        if(project.hasProperty("clean")){
            handleDeleteModuleTask(project, taskDeleteModuleAdaption)
        }else {
            project.afterEvaluate{
                if(project.hasProperty("clean")){
                    handleDeleteModuleTask(project, taskDeleteModuleAdaption)
                }
            }
        }
    }

    private void handleDeleteModuleTask(Project project, TaskProvider<ModuleDeleteAdaptionTask> taskDeleteModuleAdaption) {
        try {
            def moduleExtensionProvider = appExtension.subAdaptionPluginExtensionMapProperty.getting(project.name)
            BuildAdaptionPluginExtension me = moduleExtensionProvider.orNull
            handleDeleteModuleAdaptionTaskProperty(taskDeleteModuleAdaption.get(), appExtension, me)
            project.getTasks()
            def preBuild = project.getTasks().getByName("clean")
            preBuild.configure {
                dependsOn taskDeleteModuleAdaption.get()
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    /**
     * 添加
     * */
    private void handleModuleCreateAdaptionTask(Project project, TaskProvider<ModuleAdaptionTask> taskModuleAdaption) {
        try {
            def moduleExtensionProvider = appExtension.subAdaptionPluginExtensionMapProperty.getting(project.name)
            BuildAdaptionPluginExtension me = moduleExtensionProvider.orNull
            handleModuleAdaptionTaskProperty(taskModuleAdaption.get(), appExtension, me)
            if (!taskModuleAdaption.get().enableAdapter) {
                return
            }
            project.getTasks()
            def preBuild = project.getTasks().getByName("preBuild")
            preBuild.configure {
                dependsOn taskModuleAdaption.get()
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    static void handleModuleAdaptionTaskProperty(ModuleAdaptionTask task,BuildAdaptionPluginExtension appExtension,BuildAdaptionPluginExtension moduleExtension){
        task.needToAdaptedWidth.addAll(appExtension.needToAdaptedWidth.get())
        task.defaultDesignWidth = appExtension.defaultDesignWidth
        task.enableAdapter = appExtension.enableAdapter
        task.resPath = appExtension.resPath
        if(moduleExtension != null){
            if(moduleExtension.needToAdaptedWidth.orNull != null && !moduleExtension.needToAdaptedWidth.orNull.isEmpty()){
                task.needToAdaptedWidth = moduleExtension.needToAdaptedWidth.orNull
            }
            if(moduleExtension.defaultDesignWidth != 0){
                task.defaultDesignWidth = moduleExtension.defaultDesignWidth
            }
            task.enableAdapter = appExtension.enableAdapter && moduleExtension.enableAdapter
            if(!StringUtils.isEmpty(moduleExtension.resPath)){
                task.resPath = moduleExtension.resPath
            }
            if(moduleExtension.conversionMap.orNull != null && !moduleExtension.conversionMap.orNull.isEmpty()){
                task.conversionMap = moduleExtension.conversionMap.orNull
            }
        }
        println("handleModuleAdaptionTaskProperty ${task.name} ${task.conversionMap}  size ${task.needToAdaptedWidth.size()}")
    }

    static void handleDeleteModuleAdaptionTaskProperty(ModuleDeleteAdaptionTask task, BuildAdaptionPluginExtension appExtension, BuildAdaptionPluginExtension moduleExtension){
        task.needToAdaptedWidth.addAll(appExtension.needToAdaptedWidth.get())
        task.defaultDesignWidth = appExtension.defaultDesignWidth
        task.enableAdapter = appExtension.enableAdapter
        task.resPath = appExtension.resPath
        if(moduleExtension != null){
            if(moduleExtension.needToAdaptedWidth.orNull != null && !moduleExtension.needToAdaptedWidth.orNull.isEmpty()){
                task.needToAdaptedWidth = moduleExtension.needToAdaptedWidth.orNull
            }
            if(moduleExtension.defaultDesignWidth != 0){
                task.defaultDesignWidth = moduleExtension.defaultDesignWidth
            }
            task.enableAdapter = appExtension.enableAdapter && moduleExtension.enableAdapter
            if(!StringUtils.isEmpty(moduleExtension.resPath)){
                task.resPath = moduleExtension.resPath
            }
            println("handleModuleAdaptionTaskProperty ${task.name} ${moduleExtension.conversionMap.orNull}")
            if(moduleExtension.conversionMap.orNull != null && !moduleExtension.conversionMap.orNull.isEmpty()){
                task.conversionMap = moduleExtension.conversionMap.orNull
            }
        }
    }

}
