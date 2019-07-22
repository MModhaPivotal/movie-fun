package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private PlatformTransactionManager moviePlatformTransactionManager;
    private PlatformTransactionManager albumPlatformTransactionManager;
    private TransactionTemplate albumTransactionTemplate;
    private TransactionTemplate movieTransactionTemplate;


    @Autowired
    public void setMoviePlatformTransactionManager(PlatformTransactionManager moviePlatformTransactionManager ){
        this.moviePlatformTransactionManager = moviePlatformTransactionManager;
    }

    @Autowired
    public void setAlbumPlatformTransactionManager(PlatformTransactionManager albumPlatformTransactionManager ){
        this.albumPlatformTransactionManager = albumPlatformTransactionManager;
    }




    public HomeController(MoviesBean moviesBean,
                          AlbumsBean albumsBean,
                          MovieFixtures movieFixtures,
                          AlbumFixtures albumFixtures
                          ) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        //this.moviePlatformTransactionManager = moviePlatformTransactionManager;
        //this.albumPlatformTransactionManager = albumPlatformTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        this.albumTransactionTemplate = new TransactionTemplate(albumPlatformTransactionManager);
        this.movieTransactionTemplate = new TransactionTemplate(moviePlatformTransactionManager);



        movieTransactionTemplate.execute(new TransactionCallbackWithoutResult(){

            protected void doInTransactionWithoutResult(TransactionStatus status){
                for (Movie movie : movieFixtures.load()) {
                    moviesBean.addMovie(movie);
                }

            }

        });


        albumTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (Album album : albumFixtures.load()) {
                    albumsBean.addAlbum(album);
                }

            }
        });




        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }



}
