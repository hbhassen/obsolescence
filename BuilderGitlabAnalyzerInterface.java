package com.example.gitlab.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.gitlab.model.ProjectInfo;

public interface BuilderGitlabAnalyzerInterface {
	 /**
     * Recherche le chemin du fichier de build : pom.xml le plus proche de la racine du dépôt.
     * @param projectId identifiant du projet
     * @param files branche à analyser
     * @param projectPath chemin complet du projet (path_with_namespace)
     * @return chemin relatif du de build le plus proche, s’il existe
     */
    Optional<String> getClosestBuildfilePath( String projectPath,String buildFile,List<Map<String, Object>>files) {

    /**
     * Télécharge et décode le contenu du fichier de builde exemple pom.xml depuis GitLab.
     * @param projectId identifiant du projet
     * @param branch nom de la branche
     * @param path chemin du fichier de build
     * @return contenu texte du du fichier de build ou null si erreur
     */
    String getBuildFileContent(Long projectId, String branch, String path);
    
    List<ProjectInfo> AnalayzerBuidFile(Long projectId, String branch, String projectPath,Map<String,String> closestbuildfiles,String buildFile);


}
