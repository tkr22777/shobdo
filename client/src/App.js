import React, {Component} from 'react';
import SearchBar from './components/search-bar';
import './App.css';
import _ from 'lodash';
import axios from 'axios';

class App extends Component {
    constructor(props) {
        super(props);

        this.state = {
            searchSuggestions: []
        };

    }
    searchForSuggestions(searchTerm) {
        console.info('searching ' + searchTerm);
        axios.post('http://172.17.0.1:9000/dict/search', {
            spelling: searchTerm
          })
          .then(function (response) {
            console.log(response);
          })
          .catch(function (error) {
            console.log(error);
          });
    }

    render() {
        const search = _.debounce((searchTerm) => {
            this.searchForSuggestions(searchTerm)
        }, 300);
        return (
            <div>
                <h1>Search</h1>
                <SearchBar onSearchTermChange={searchTerm => search(searchTerm)}/>
            </div>

        );
    }
}

export default App;
